package com.builtbroken.mc.patch;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Core mod for Voltz Engine handing anything that needs to be done before mods load.
 * Created by Dark on 9/7/2015.
 * -Dfml.coreMods.load=com.builtbroken.mc.core.EngineCoreMod
 */
@IFMLLoadingPlugin.MCVersion(value = "1.7.10")
public class CoreMod implements IFMLLoadingPlugin
{
    /** Grab the mod's main logger, in theory should be the same logger */
    public static final Logger logger = LogManager.getLogger("BBM-MC-PATCHER");
    public static boolean devMode = false;

    public CoreMod()
    {
        devMode = System.getProperty("development") == null || !System.getProperty("development").equalsIgnoreCase("true");
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
}
