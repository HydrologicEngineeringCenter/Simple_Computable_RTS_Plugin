import com.rma.factories.AbstractNewObjectFactory;
import com.rma.factories.NewObjectFactory;
import com.rma.io.FileManagerImpl;
import com.rma.io.RmaFile;
import com.rma.model.Project;
import com.rma.ui.GenericNewObjectPanel;
import com.rma.util.I18n;

import javax.swing.*;

public class ComputableAltFactory extends AbstractNewObjectFactory implements NewObjectFactory {
//    Need to modify this constructor later
    private ComputableMain _plugin;
    public ComputableAltFactory(ComputableMain plugin) {
        super(ComputableI18n.getI18n(ComputableMessages.Plugin_Name));
        _plugin = plugin;
    }

    //    Need to add a constructor
    @Override
    public JComponent createNewObjectPanel() {
        GenericNewObjectPanel panel = new GenericNewObjectPanel();
        panel.setFileComponentsVisible(false);
        Project p = Project.getCurrentProject();
        panel.setName("");
        panel.setDescription("");
        panel.setExistingNamesList(_plugin.getAlternativeList());
        panel.setDirectory(p.getProjectDirectory() + RmaFile.separator + _plugin.getPluginDirectory());
        return panel;
    }

    @Override
    public Object createObject(JComponent jc) {
        GenericNewObjectPanel panel = (GenericNewObjectPanel) jc;
        ComputableAlt alt = new ComputableAlt();
        alt.setName(panel.getSelectedName());
        alt.setDescription(panel.getSelectedDescription());
        alt.setFile(FileManagerImpl.getFileManager().getFile(panel.getSelectedFile().getPath() + RmaFile.separator + alt.getName() + _plugin.getAltFileExtension()));
        alt.setProject(Project.getCurrentProject());
        _plugin.addAlternative(alt);
        _plugin.editAlternative(alt);
        alt.saveData();
        return alt;

    }
    @Override
    public void openObject(JComponent jc) {

    }
    @Override
    public JComponent createOpenObjectPanel() {
        return null;
    }
}
