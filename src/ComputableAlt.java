import com.rma.io.DssFileManagerImpl;
import com.rma.io.RmaFile;
import hec.heclib.dss.HecDSSDataAttributes;
import hec.io.DSSIdentifier;
import hec.io.TimeSeriesContainer;
import hec2.model.DataLocation;
import hec2.plugin.PathnameUtilities;
import hec2.plugin.model.ComputeOptions;
import hec2.plugin.model.ModelAlternative;
import hec2.plugin.selfcontained.SelfContainedPluginAlt;
import org.jdom.Document;
import org.jdom.Element;
import hec.heclib.dss.DSSPathname;


import java.sql.Time;
import java.util.ArrayList;
import java.util.List;


public class ComputableAlt extends SelfContainedPluginAlt {
    private List<DataLocation> _dataLocations;
    private static final String DocumentRoot = "ComputableAlt";
    private static final String AlternativeNameAttribute = "Name";
    private static final String AlternativeDescriptionAttribute = "Desc";
    private ComputeOptions _computeOptions;

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
    public void setComputeOptions (ComputeOptions opts){
        _computeOptions = opts;
    }

    @Override
    public boolean isComputable() {
        return true;
    }

    @Override
    public boolean compute() {
        boolean returnValue = true;
        hec2.rts.model.ComputeOptions cco = (hec2.rts.model.ComputeOptions) _computeOptions;
        double multiplier = 2.0;
        String dssFilePath = cco.getDssFilename();
        for (DataLocation dl : _dataLocations) {
            String dssPath = dl.getLinkedToLocation().getDssPath();
//            read input TS
            TimeSeriesContainer tsc = ReadInputTS(dssFilePath, dssPath);
//            multiply input data
            TimeSeriesContainer output = UpdateTS(tsc, multiplier);
//            write output data
            if (!WriteOutTS(output, dl, dssFilePath)) {
                returnValue = false;
            }
            return returnValue;
        }
        return false;
    }



    private TimeSeriesContainer UpdateTS(TimeSeriesContainer input, double multiplier) {
        TimeSeriesContainer outTSC = (TimeSeriesContainer)input.clone();
        double[] vals = outTSC.values;
        for (int i = 0; i < (vals.length); i++) {
            vals[i] = vals[i] * multiplier;
        }
        outTSC.values = vals;
        return outTSC;
    }


    private TimeSeriesContainer ReadInputTS(String DssFilePath, String dssPath) {
        DSSPathname pathName = new DSSPathname(dssPath);
        String InputFPart = pathName.getFPart();
        DSSIdentifier eventDss = new DSSIdentifier(DssFilePath, pathName.getPathname());
        eventDss.setStartTime(_computeOptions.getRunTimeWindow().getStartTime());
        eventDss.setEndTime(_computeOptions.getRunTimeWindow().getEndTime());
        int type = DssFileManagerImpl.getDssFileManager().getRecordType(eventDss);
        if((HecDSSDataAttributes.REGULAR_TIME_SERIES<=type && type < HecDSSDataAttributes.PAIRED)){
            boolean exist = DssFileManagerImpl.getDssFileManager().exists(eventDss);
            TimeSeriesContainer eventTsc = null;
            if (!exist )
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            eventTsc = DssFileManagerImpl.getDssFileManager().readTS(eventDss, true);
            if ( eventTsc != null )
            {
                exist = eventTsc.numberValues > 0;
            }
            if(exist){
                return eventTsc;
            }else{
                return null;
            }
        }else {
            return null;
        }

    }
    public boolean WriteOutTS(TimeSeriesContainer tsc, DataLocation dl, String dssFilePath){
        DSSPathname pathname = new DSSPathname(dl.getDssPath());
        pathname.setFPart(_computeOptions.getFpart());
        DSSIdentifier eventDss = new DSSIdentifier(dssFilePath,pathname.getPathname());
        eventDss.setStartTime(_computeOptions.getRunTimeWindow().getStartTime());
        eventDss.setEndTime(_computeOptions.getRunTimeWindow().getEndTime());
        tsc.fullName = pathname.getPathname();
        tsc.fileName = _computeOptions.getDssFilename();
        boolean exist = DssFileManagerImpl.getDssFileManager().exists(eventDss);
        if(exist){
            if(!_computeOptions.shouldForceCompute()){
                return true;
            }
        }
        return 0 == DssFileManagerImpl.getDssFileManager().write(tsc);

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

    public boolean setDataLocations(List<DataLocation> dataLocations) {
        boolean retval = false;
        for(DataLocation dl : dataLocations){
            if(!_dataLocations.contains(dl)){
                DataLocation linkedTo = dl.getLinkedToLocation();
                String dssPath = linkedTo.getDssPath();
                if(validLinkedToDssPath(dl))
                {
                    setModified(true);
                    setDssParts(dl);
                    _dataLocations.add(dl);
                    retval = true;
                }
            }else{
                DataLocation linkedTo = dl.getLinkedToLocation();
                String dssPath = linkedTo.getDssPath();
                if(validLinkedToDssPath(dl))
                {
                    setModified(true);
                    setDssParts(dl);
                    retval = true;
                }
            }
        }
        if(retval)saveData();
        return retval;
    }
}

