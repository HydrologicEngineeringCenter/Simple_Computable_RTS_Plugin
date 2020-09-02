import com.rma.factories.NewObjectFactory;
import com.rma.io.RmaFile;
import hec2.map.GraphicElement;
import hec2.model.DataLocation;
import hec2.model.ProgramOrderItem;
import hec2.plugin.CreatablePlugin;
import hec2.plugin.action.EditAction;
import hec2.plugin.action.OutputElement;
import hec2.plugin.lang.ModelLinkingException;
import hec2.plugin.lang.OutputException;
import hec2.plugin.model.ComputeOptions;
import hec2.plugin.model.ModelAlternative;
import hec2.plugin.selfcontained.AbstractSelfContainedPlugin;
import hec2.rts.plugin.RtsPlugin;
import hec2.rts.plugin.RtsPluginManager;
import hec2.rts.plugin.action.ComputeModelAction;
import hec2.rts.ui.RtsTabType;
import rma.util.RMAFilenameFilter;
import rma.util.RMAIO;
import com.rma.io.FileManagerImpl;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;


public class ComputableMain extends AbstractSelfContainedPlugin<ComputableAlt> implements RtsPlugin, CreatablePlugin {
    //    cant have spaces in PluginName. PluginName will show in in model and forecast tree
    public static final String PluginName = "SCP";
    private static final String _pluginVersion = "1.0.0";
    //    this is the name of the directory model data will be stored (ex. rss)
    private static final String _pluginSubDirectory = "scp";
    //    the extension for plugin files (like the alternative)
    private static final String _pluginExtension = ".scp";

    public static void main(String[] args) {

        ComputableMain p = new ComputableMain();
    }

    public ComputableMain() {
        super();
        setName(PluginName);
        setProgramOrderItem(new ProgramOrderItem(PluginName,
                "A plugin constructed from the tutorial",
                false, 1, "SCP", "Images/riverware/png/WaterUser16.png"));
        RtsPluginManager.register(this);
    }

    @Override
    public void editAlternative(ComputableAlt computableAlt) {

    }

    @Override
    protected ComputableAlt newAlternative(String s) {
        return new ComputableAlt(s);
    }

    @Override
    protected String getAltFileExtension() {
        return _pluginExtension;
    }

    @Override
    public String getPluginDirectory() {
        return _pluginSubDirectory;
    }

    @Override
    protected NewObjectFactory getAltObjectFactory() {
        return new ComputableAltFactory(this);
    }

    @Override
    public boolean compute(ModelAlternative ma) {
        ComputableAlt alt = getSimulationAlt(ma);
        if (alt != null) {
//            What is ths line doing?
            alt.setComputeOptions(ma.getComputeOptions());
            if (_computeListeners != null && !_computeListeners.isEmpty()) {
                for (int i = 0; i < _computeListeners.size(); i++) {
                    alt.addComputeListener(_computeListeners.get(i));
                }
            }
            addComputeMessage("Starting Compute For " + alt.getName());
            return alt.compute();
        } else {
            addComputeErrorMessage("Failed to find Alternative for " + ma);
            return false;
        }
    }

    @Override
    public List<DataLocation> getDataLocations(ModelAlternative ma, int i) {
        List<DataLocation> emplist = new ArrayList<>();
        if (ma == null) {
            System.out.println("getDataLocations: No ModelAlternative found");
            return emplist;
        }
        ComputableAlt alt;
        if (ma.getComputeOptions() != null) {
            alt = getSimulationAlt(ma);
        } else {
            alt = getAlt(ma);
        }
        if (DataLocation.INPUT_LOCATIONS == i) {
            //input
            return alt.getInputDataLocations();
        } else {
            //output
            return alt.getOutputDataLocations();
        }
    }

    @Override
    public boolean setDataLocations(ModelAlternative ma, List<DataLocation> list) throws ModelLinkingException {
        ComputableAlt alt;
        if (ma.getComputeOptions() != null) {
            alt = getSimulationAlt(ma);
        } else {
            alt = getAlt(ma);
        }
        if (alt != null) {
            return alt.setDataLocations(list);
        }
        return false;
    }

    @Override
    public List<GraphicElement> getGraphicElements(ModelAlternative ma) {
        return null;
    }

    @Override
    public List<OutputElement> getOutputReports(ModelAlternative ma) {
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
    public List<EditAction> getEditActions(ModelAlternative ma) {
        List<EditAction> actions = new ArrayList<EditAction>();
        ComputeModelAction cation = new ComputeModelAction("Compute", PluginName, "computeModel");
        actions.add(cation);
        return actions;
    }

    @Override
    public void editAction(String s, ModelAlternative ma) {
    }

    @Override
    public boolean saveProject() {
        boolean success = true;
        for (ComputableAlt alt : _altList) {
            if (!alt.saveData()) {
                success = false;
                System.out.println("Alternative " + alt.getName() + " could not save.");
            }
        }
        return success;
    }

    @Override
    public boolean displayApplication() {
        return false;
    }

    @Override
    public String getVersion() {
        return _pluginVersion;
    }

    @Override
    public boolean copyModelFiles(ModelAlternative ma, String s, boolean b) {
        String source = RMAIO.concatPath(s, _pluginSubDirectory);
        String dest;
        RMAFilenameFilter filt = new RMAFilenameFilter("bak");
        if (b) {
//            for forecast creation and replace from base
            dest = RMAIO.concatPath(ma.getRunDirectory(), getPluginDirectory());
            FileManagerImpl.getFileManager().copyDirectory(source, dest, filt, null);
            return true;
        } else {
//            for copy to base
            dest = getDirectory();
        }
        FileManagerImpl.getFileManager().copyDirectory(source, dest, filt, null);
        return true;
    }

    @Override
    public List<EditAction> getGlobalEditActions(RtsTabType rtsTabType) {
        return null;
    }

    @Override
    public boolean closeForecast(String s) {
        return false;
    }

    public ComputableAlt getSimulationAlt(ModelAlternative ma) {
        if (ma == null)
        {
            return null;
        }
        ComputeOptions co = ma.getComputeOptions();
        if (co == null)
        {
            return null;
        }
        ComputableAlt alt = getAlt(ma);
        if (alt == null)
        {
            return null;
        }
        String altName = ma.getName();
        RmaFile file = alt.getFile();
        String runDir = getRunDirectory(co);
        String fname = file.getName();
        String runPath = runDir.concat(RmaFile.separator).concat(fname);
        RmaFile runFile = FileManagerImpl.getFileManager().getFile(runPath);
        ComputableAlt simAlt = newAlternative(runFile.getAbsolutePath());
        simAlt.setFile(runFile);
        simAlt.setProject(alt.getProject());
        simAlt.setName(altName);
        simAlt.readData();
        return simAlt;

    }
}