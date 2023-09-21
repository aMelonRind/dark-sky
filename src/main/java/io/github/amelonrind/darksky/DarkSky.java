package io.github.amelonrind.darksky;

import io.github.amelonrind.darksky.config.Config;
import net.fabricmc.api.ClientModInitializer;

public class DarkSky implements ClientModInitializer {

    public static final String MOD_ID = "dark-sky";

    public static Config config = Config.read().apply();

    @Override
    public void onInitializeClient() {}

}
