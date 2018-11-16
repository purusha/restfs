package it.at.restfs.guice;

import akka.actor.*;

public class GuiceExtension extends AbstractExtensionId<GuiceExtensionImpl> implements ExtensionIdProvider {

    public static final GuiceExtension provider = new GuiceExtension();

    @Override
    public GuiceExtensionImpl createExtension(ExtendedActorSystem system) {
        return new GuiceExtensionImpl();
    }

    @Override
    public ExtensionId<? extends Extension> lookup() {
        return provider;
    }
}