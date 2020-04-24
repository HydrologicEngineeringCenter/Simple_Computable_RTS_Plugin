import hec2.plugin.selfcontained.SelfContainedPluginAlt;
import org.jdom.Document;

public class ComputableAlt extends SelfContainedPluginAlt {

    @Override
    protected boolean loadDocument(Document document) {
        return false;
    }

    @Override
    public boolean isComputable() {
        return false;
    }

    @Override
    public boolean compute() {
        return false;
    }

    @Override
    public int getModelCount() {
        return 0;
    }

    @Override
    public boolean cancelCompute() {
        return false;
    }

    @Override
    public String getLogFile() {
        return null;
    }
}

