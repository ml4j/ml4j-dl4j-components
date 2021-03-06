/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ml4j.nn.dl4j.factories;

import org.ml4j.MatrixFactory;
import org.ml4j.nn.activationfunctions.ActivationFunctionBaseType;
import org.ml4j.nn.activationfunctions.ActivationFunctionProperties;
import org.ml4j.nn.activationfunctions.ActivationFunctionType;
import org.ml4j.nn.activationfunctions.DifferentiableActivationFunction;
import org.ml4j.nn.axons.factories.AxonsFactory;
import org.ml4j.nn.components.DirectedComponentsContext;
import org.ml4j.nn.components.activationfunctions.DifferentiableActivationFunctionComponent;
import org.ml4j.nn.dl4j.activationfunctions.DL4JDifferentiableActivationFunctionComponentImpl;
import org.ml4j.nn.factories.DefaultDirectedComponentFactoryImpl;
import org.ml4j.nn.neurons.Neurons;
import org.ml4j.nn.neurons.NeuronsActivationFeatureOrientation;
import org.ml4j.provider.Provider;
import org.ml4j.provider.enums.activationfunctions.ActivationFunctionTypeEnum;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.activations.IActivation;
import org.nd4j.linalg.activations.impl.ActivationLReLU;

/**
 * Extension of the default DefaultDirectedComponentFactoryImpl from ML4J which
 * uses DL4J components equivalents for some functionality.
 * 
 * Currently implemented so that activation functions from DL4J are used, while
 * other components are loaded from ML4J
 * 
 * @author Michael Lavelle
 */
public class HybridML4JDL4JDefaultDirectedComponentFactory extends DefaultDirectedComponentFactoryImpl {

	
	
	/**
	 * Default serialization id.
	 */
	private static final long serialVersionUID = 1L;

	public HybridML4JDL4JDefaultDirectedComponentFactory(MatrixFactory matrixFactory, AxonsFactory axonsFactory, DirectedComponentsContext directedComponentsContext) {
		super(matrixFactory, axonsFactory, null, directedComponentsContext);
	}

	@Override
	public DifferentiableActivationFunctionComponent createDifferentiableActivationFunctionComponent(String name, Neurons neurons,
			DifferentiableActivationFunction differentiableActivationFunction) {
		return createDifferentiableActivationFunctionComponent(name, neurons,
				differentiableActivationFunction.getActivationFunctionType(), differentiableActivationFunction.getActivationFunctionProperties());
	}

	@Override
	public DifferentiableActivationFunctionComponent createDifferentiableActivationFunctionComponent(String name, Neurons neurons,
			ActivationFunctionType activationFunctionType, ActivationFunctionProperties activationFunctionProperties) {
		
		// Find the provider-agnostic ActivationFunctionTypeEnum from this ml4j-specific
		// type
		ActivationFunctionTypeEnum activationFunctionTypeEnum = ActivationFunctionTypeEnum
				.findByQualifiedEnumName(activationFunctionType.getQualifiedId()).orElseThrow(() -> new IllegalArgumentException(
						"Cannot find provider-agnostic activation function type for:" + activationFunctionType));		
		
		// Get the dl4j equivalent enum
		Activation dl4jActivationFunctionType = activationFunctionTypeEnum.providedBy(Provider.DL4J)
				.getEnumAsType(Activation.class);

		NeuronsActivationFeatureOrientation requiredOrientation = activationFunctionType
				.getBaseType() == ActivationFunctionBaseType.SOFTMAX
						? NeuronsActivationFeatureOrientation.COLUMNS_SPAN_FEATURE_SET
						: null;
		IActivation activationFunction = createActivationFunction(dl4jActivationFunctionType, activationFunctionProperties);
		return new DL4JDifferentiableActivationFunctionComponentImpl(name, neurons,
				activationFunction, activationFunctionType, requiredOrientation);

	}
	
	private IActivation createActivationFunction(Activation dl4jActivationFunctionType, ActivationFunctionProperties activationFunctionProperties) {
		if (dl4jActivationFunctionType == Activation.LEAKYRELU && activationFunctionProperties.getAlpha().isPresent()) {
			return new ActivationLReLU(activationFunctionProperties.getAlpha().get().floatValue());
		} else {
			return dl4jActivationFunctionType.getActivationFunction();
		}
	}

}
