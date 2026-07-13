package it.gov.pagopa.mock.utils;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class JUnitExtensionContextHolder implements BeforeAllCallback {
    public static ExtensionContext extensionContext;
    @Override
    public void beforeAll(ExtensionContext context) {
        extensionContext = context;
    }
}