import com.rma.factories.NewObjectFactory;
import hec2.map.GraphicElement;
import hec2.model.DataLocation;
import hec2.model.ProgramOrderItem;
import hec2.plugin.action.EditAction;
import hec2.plugin.action.OutputElement;
import hec2.plugin.lang.ModelLinkingException;
import hec2.plugin.lang.OutputException;
import hec2.plugin.model.ModelAlternative;
import hec2.plugin.selfcontained.AbstractSelfContainedPlugin;
import hec2.rts.plugin.RtsPlugin;
import hec2.rts.plugin.RtsPluginManager;
import hec2.rts.ui.RtsTabType;

import java.util.List;

public class ComputableMain extends AbstractSelfContainedPlugin<ComputableAlt> implements RtsPlugin {

    public static final String PluginName = "Simple Computable Plugin";
    private static final String _pluginVersion = "1.0.0";
    private static final String _pluginSubDirectory = "ComputablePlugin";
    private static final String _pluginExtension = ".scp";

    public static void main(String[] args) {

        ComputableMain p = new ComputableMain();
    }

    public ComputableMain() {
        super();
        setName(PluginName);
        setProgramOrderItem(new ProgramOrderItem(PluginName,
                "A plugin constructed from the tutorial",
                false, 1, "EMT", "Images/fda/wsp.png"));
        RtsPluginManager.register(this);
    }
    @Override
    public void editAlternative(ComputableAlt computableAlt) {

    }

    @Override
    protected ComputableAlt newAlternative(String s) {
        return null;
    }

    @Override
    protected String getAltFileExtension() {
        return null;
    }

    @Override
    public String getPluginDirectory() {
        return null;
    }

    @Override
    protected NewObjectFactory getAltObjectFactory() {
        return null;
    }

    @Override
    public boolean compute(ModelAlternative modelAlternative) {
        return false;
    }

    @Override
    public List<DataLocation> getDataLocations(ModelAlternative modelAlternative, int i) {
        return null;
    }

    @Override
    public boolean setDataLocations(ModelAlternative modelAlternative, List<DataLocation> list) throws ModelLinkingException {
        return false;
    }

    @Override
    public List<GraphicElement> getGraphicElements(ModelAlternative modelAlternative) {
        return null;
    }

    @Override
    public List<OutputElement> getOutputReports(ModelAlternative modelAlternative) {
        return null;
    }

    @Override
    public boolean displayEditor(GraphicElement graphicElement) {
        return false;
    }

    @Override
    public boolean displayOutput(OutputElement outputElement, List<ModelAlternative> list) throws OutputException {
        return false;
    }

    @Override
    public List<EditAction> getEditActions(ModelAlternative modelAlternative) {
        return null;
    }

    @Override
    public void editAction(String s, ModelAlternative modelAlternative) {

    }

    @Override
    public boolean saveProject() {
        return false;
    }

    @Override
    public boolean displayApplication() {
        return false;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public boolean copyModelFiles(ModelAlternative modelAlternative, String s, boolean b) {
        return false;
    }

    @Override
    public List<EditAction> getGlobalEditActions(RtsTabType rtsTabType) {
        return null;
    }

    @Override
    public boolean closeForecast(String s) {
        return false;
    }
}
