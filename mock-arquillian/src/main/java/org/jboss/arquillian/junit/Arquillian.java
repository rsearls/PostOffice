package org.jboss.arquillian.junit;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

// mock impl to avoid actural impl
public class Arquillian extends BlockJUnit4ClassRunner {

    public Arquillian(Class<?> testClass) throws InitializationError {
        super(testClass);
    }
}