import com.rma.io.RmaFile;
import hec2.model.DataLocation;
import hec2.plugin.PathnameUtilities;
import hec2.plugin.model.ModelAlternative;
import hec2.plugin.selfcontained.SelfContainedPluginAlt;
import org.jdom.Document;
import org.jdom.Element;
import hec.heclib.dss.DSSPathname;


import java.util.ArrayList;
import java.util.List;


public class ComputableAlt extends SelfContainedPluginAlt {
    private List<DataLocation> _dataLocations;
    private static final String DocumentRoot = "ComputableAlt";
    private static final String AlternativeNameAttribute = "Name";
    private static final String AlternativeDescriptionAttribute = "Desc";

    public ComputableAlt(){
        super();
        _dataLocations = new ArrayList<>();
    }

    public ComputableAlt(String name){
        this();
        setName(name);
    }
    @Override
    public boolean saveData(RmaFile file) {
        if (file != null) {
            Element root = new Element(DocumentRoot);
            root.setAttribute(AlternativeNameAttribute, getName());
            root.setAttribute(AlternativeDescriptionAttribute, getDescription());
            if (_dataLocations != null)
            {
                saveDataLocations(root, _dataLocations);
            }
            Document doc = new Document(root);
            return writeXMLFile(doc, file);
        }
        return false;
    }
    @Override
    protected boolean loadDocument(org.jdom.Document dcmnt) {
        if (dcmnt!=null){
            org.jdom.Element ele = dcmnt.getRootElement();
            if (ele ==null){
                System.out.println("No root element on the provided XML Document");
                return false;
            }
            if (ele.getName().equals(DocumentRoot)){
                setName(ele.getAttributeValue(AlternativeNameAttribute));
                setDescription(ele.getAttributeValue(AlternativeDescriptionAttribute));

            }
            else{
                System.out.println("XML document root was improperly named. ");
                return false;
            }
            if( _dataLocations==null){
                _dataLocations = new ArrayList<>();
            }
            _dataLocations.clear();
            loadDataLocations(ele, _dataLocations);
            setModified(false);
            return true;
        }
        else {
            System.out.println("WML Document was null.");
            return false;
        }
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
    private List<DataLocation> defaultDataLocations() {
        if (!_dataLocations.isEmpty()) {
            //locations have previously been set (most likely from reading
            //in an existing alternative file.
            for (DataLocation dl : _dataLocations) {
                String dlparts = dl.getDssPath();
                DSSPathname p = new DSSPathname(dlparts);
                if (p.aPart().equals("") && p.bPart().equals("") && p.cPart().equals("") && p.dPart().equals("") && p.ePart().equals("") && p.fPart().equals("")) {
                    if (validLinkedToDssPath(dl)) {
                        setDssParts(dl);
                    }
                }
            }
            return _dataLocations;
        }
        List<DataLocation> dlList = new ArrayList<>();
        //create a default location so that links can be initialized.
        DataLocation dloc = new DataLocation(this.getModelAlt(), _name, "Any");
        dlList.add(dloc);
        return dlList;
    }
    private boolean validLinkedToDssPath(DataLocation dl) {
        DataLocation linkedTo = dl.getLinkedToLocation();
        String dssPath = linkedTo.getDssPath();
        return !(dssPath == null || dssPath.isEmpty());
    }
    private void setDssParts(DataLocation dl) {
        DataLocation linkedTo = dl.getLinkedToLocation();
        String dssPath = linkedTo.getDssPath();
        DSSPathname p = new DSSPathname(dssPath);
        String[] parts = p.getParts();
        parts[1] = parts[1] + " Output";
        ModelAlternative malt = this.getModelAlt();
        malt.setProgram(ComputableMain.PluginName);
        parts[5] = "C000000:" + _name + ":" + PathnameUtilities.getWatFPartModelPart(malt);
        p.setParts(parts);
        dl.setDssPath(p.getPathname());
    }
        public List<DataLocation> getInputDataLocations() {
        return defaultDataLocations();
    }

    public List<DataLocation> getOutputDataLocations() {
        return defaultDataLocations();
    }
}

