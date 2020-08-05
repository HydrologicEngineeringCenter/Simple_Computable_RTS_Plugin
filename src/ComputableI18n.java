import com.rma.util.I18n;

import java.util.ResourceBundle;

public class ComputableI18n extends I18n {
//    String bundle name not consistent with WAT example because not working in a package.
    public static final String BUNDLE_NAME = "ComputableProperties";
    private static final ResourceBundle SAMPLE_RESOURCE_BUNDLE;
    private ResourceBundle _resourceBundle;
    static
    {
        SAMPLE_RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
    }
    protected ComputableI18n(String prefix, String bundleName)
    {
        super(prefix, bundleName);
    }
    public static I18n getI18n(String prefix)
    {
        return new ComputableI18n(prefix, BUNDLE_NAME);
    }
}
