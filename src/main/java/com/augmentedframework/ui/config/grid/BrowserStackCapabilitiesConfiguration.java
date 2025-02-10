package com.augmentedframework.ui.config.grid;

import com.augmentedframework.utils.Log;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.lang.reflect.*;
import java.util.*;

/**
 * @author YSabato
 * @version 1.0
 * @since 11/20/2024
 */

@Getter
@Setter
public class BrowserStackCapabilitiesConfiguration implements IGridCapabilitiesConfiguration {

    // BrowserStack-specific capabilities
    private String os;
    private String os_version;
    private String browser;
    private String browser_version;

    // Test configuration capabilities
    private String projectName;
    private String build;
    private String name;
    private String local;
    private String localIdentifier;
    private String debug;
    private String consoleLogs;
    private String networkLogs;
    private String appiumLogs;
    private String video;
    private String seleniumLogs;
    private String geoLocation;
    private String timezone;
    private String resolution;
    private String selenium_version;
    private String[] maskCommands;

    private String idleTimeout;

    @Override
    public DesiredCapabilities getDesiredCapabilities() {
        DesiredCapabilities caps = new DesiredCapabilities();
        setBrowserStackOptions(caps);
        return caps;
    }

    private void setBrowserStackOptions(DesiredCapabilities caps) {
        List<String> capNotToInclude = Arrays.asList("idleTimeout", "local", "consoleLogs", "networkLogs");
        List<Field> fields = new ArrayList<>(Arrays.asList(this.getClass().getDeclaredFields()));
        fields.forEach(field -> {
            try {
                if (field.get(this) != null && !capNotToInclude.contains(field.getName())) {
                    caps.setCapability(field.getName(), (String) field.get(this));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        caps.setCapability("browserstack.idleTimeout", idleTimeout);
        boolean local = System.getProperty("local") != null ? Boolean.valueOf(System.getProperty("local")) : this.local != null ? Boolean.valueOf(this.local): false;
        boolean captureNetworkLogs = System.getProperty("networkLogs") != null ? Boolean.valueOf(System.getProperty("networkLogs")) : this.networkLogs != null ? Boolean.valueOf(this.networkLogs) : false;
        boolean captureConsoleLogs = this.consoleLogs != null ? Boolean.valueOf(this.consoleLogs) : false;
        caps.setCapability("browserstack.local", local);
        caps.setCapability("browserstack.networkLogs", captureNetworkLogs);
        caps.setCapability("browserstack.consoleLogs", captureConsoleLogs);
    }

    public void override(Map<String, String> capsToOverride) {
        capsToOverride.forEach((cap, value) -> {
            try {
                invokeSetter(cap, value);
            } catch (IllegalAccessException e) {
                Log.message(String.format("Illegal capability %s found.", cap));
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                Log.message("Unable to override capability. " + e.getMessage());
            }
        });
    }

    private void invokeSetter(String fieldName, String value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String setterName = "set" + fieldName.toUpperCase().charAt(0) + fieldName.substring(1);
        Method setter = null;
        try {
            setter = this.getClass().getMethod(setterName, String.class);
        } catch(Exception e) {
            // MaskCommands takes string[] hence it is getting failed. Handling that using String[]
            setter = this.getClass().getMethod(setterName, String[].class);
        }

        setter.invoke(this, value);
    }
}
