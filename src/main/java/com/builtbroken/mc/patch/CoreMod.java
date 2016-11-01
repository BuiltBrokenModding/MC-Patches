package com.builtbroken.mc.patch;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Core mod for loading ASM transformations
 * Created by Dark on 10/9/2016.
 * -Dfml.coreMods.load=com.builtbroken.mc.patch.CoreMod
 */
@IFMLLoadingPlugin.MCVersion(value = "1.7.10")
@IFMLLoadingPlugin.Name(value = "mc-patch")
public class CoreMod implements IFMLLoadingPlugin
{
    //TODO provide some kind of config to disable patches
    //TODO provide an agreement to user's in understanding edits are applied at own risk

    /** Grab the mod's main logger, in theory should be the same logger */
    public static final Logger logger = LogManager.getLogger("BBM-MC-PATCHER");

    public CoreMod()
    {
        logger.info("CoreMod has been created");
    }

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[]{"com.builtbroken.mc.patch.ClassTransformer"};
    }

    @Override
    public String getModContainerClass()
    {
        return null;
    }

    @Override
    public String getSetupClass()
    {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data)
    {

    }

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }

    /**
     * Checks if the program is running in development mode.
     * This is normally used to enable additional debug such
     * as printing edited classes to file between runs.
     *
     * @return true if system arguments contain -Ddevelopmenet=true
     */
    public static boolean isDevMode()
    {
        return System.getProperty("development") != null && System.getProperty("development").equalsIgnoreCase("true");
    }
}
