import com.rma.io.DssFileManagerImpl;
import com.rma.io.RmaFile;
import hec.heclib.dss.HecDSSDataAttributes;
import hec.io.DSSIdentifier;
import hec.io.TimeSeriesContainer;
import hec2.model.DataLocation;
import hec2.plugin.model.ComputeOptions;
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
    private ComputeOptions _computeOptions;

    public ComputableAlt() {
        super();
        _dataLocations = new ArrayList<>();
    }

    public ComputableAlt(String name) {
        this();
        setName(name);
    }

    @Override
    public boolean saveData(RmaFile file) {
        if (file != null) {
            Element root = new Element(DocumentRoot);
            root.setAttribute(AlternativeNameAttribute, getName());
            root.setAttribute(AlternativeDescriptionAttribute, getDescription());
            if (_dataLocations != null) {
                saveDataLocations(root, _dataLocations);
            }
            Document doc = new Document(root);
            return writeXMLFile(doc, file);
        }
        return false;
    }

    @Override
    protected boolean loadDocument(org.jdom.Document dcmnt) {
        if (dcmnt != null) {
            org.jdom.Element ele = dcmnt.getRootElement();
            if (ele == null) {
                System.out.println("No root element on the provided XML Document");
                return false;
            }
            if (ele.getName().equals(DocumentRoot)) {
                setName(ele.getAttributeValue(AlternativeNameAttribute));
                setDescription(ele.getAttributeValue(AlternativeDescriptionAttribute));

            } else {
                System.out.println("XML document root was improperly named. ");
                return false;
            }
            if (_dataLocations == null) {
                _dataLocations = new ArrayList<>();
            }
            _dataLocations.clear();
            loadDataLocations(ele, _dataLocations);
            setModified(false);
            return true;
        } else {
            System.out.println("WML Document was null.");
            return false;
        }
    }

    public void setComputeOptions(ComputeOptions opts) {
        _computeOptions = opts;
    }

    @Override
    public boolean isComputable() {
        return true;
    }

    @Override
    public boolean compute() {
        boolean returnValue = true;
//        is casting to hec2.rts.model.ComputeOptions required?_computeOptions is of type hec2.plugin.model.ComputeOptions
//        hec2.rts.model.ComputeOptions extends hec2.plugin.model.ComputeOptions
        hec2.rts.model.ComputeOptions cco = (hec2.rts.model.ComputeOptions) _computeOptions;
        double multiplier = 2.0;
        String dssFilePath = cco.getDssFilename();
        for (DataLocation dl : _dataLocations) {
            String dssPath = dl.getLinkedToLocation().getDssPath();
//            read input TS
            TimeSeriesContainer tsc = readInputTS(dssFilePath, dssPath);
            if (tsc == null) {
                addComputeErrorMessage("The DSS pathname provided " + dssPath + " was not found in " + dssFilePath);
                return false;
            }
//            multiply input data
            TimeSeriesContainer output = updateTS(tsc, multiplier);
//            write output data
            if (!writeOutTS(output, dl, dssFilePath)) {
                addComputeErrorMessage("Could not write to " + output.getFullName() + " in " + dssFilePath);
                returnValue = false;
            }
        }
        return returnValue;
    }

    private TimeSeriesContainer readInputTS(String DssFilePath, String dssPath) {
        DSSPathname pathName = new DSSPathname(dssPath);
        String InputFPart = pathName.getFPart();
        DSSIdentifier forecastDSS = new DSSIdentifier(DssFilePath, pathName.getPathname());
        forecastDSS.setStartTime(_computeOptions.getRunTimeWindow().getStartTime());
        forecastDSS.setEndTime(_computeOptions.getRunTimeWindow().getEndTime());
        int type = DssFileManagerImpl.getDssFileManager().getRecordType(forecastDSS);
        addComputeMessage("Reading " + dssPath + " from" + DssFilePath);
        if ((HecDSSDataAttributes.REGULAR_TIME_SERIES <= type && type < HecDSSDataAttributes.PAIRED)) {
            boolean exist = DssFileManagerImpl.getDssFileManager().exists(forecastDSS);
            TimeSeriesContainer fcstTsc = null;
            if (!exist) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            fcstTsc = DssFileManagerImpl.getDssFileManager().readTS(forecastDSS, true);
            if (fcstTsc != null) {
                exist = fcstTsc.numberValues > 0;
            }
            if (exist) {
                return fcstTsc;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private TimeSeriesContainer updateTS(TimeSeriesContainer input, double multiplier) {
        TimeSeriesContainer outTSC = (TimeSeriesContainer) input.clone();
        double[] vals = outTSC.values;
        for (int i = 0; i < (vals.length); i++) {
            vals[i] = vals[i] * multiplier;
        }
        outTSC.values = vals;
        return outTSC;
    }

    public boolean writeOutTS(TimeSeriesContainer tsc, DataLocation dl, String dssFilePath) {
        DSSPathname pathname = new DSSPathname(dl.getDssPath());
        pathname.setFPart(_computeOptions.getFpart());
        DSSIdentifier forecastDSS = new DSSIdentifier(dssFilePath, pathname.getPathname());
        forecastDSS.setStartTime(_computeOptions.getRunTimeWindow().getStartTime());
        forecastDSS.setEndTime(_computeOptions.getRunTimeWindow().getEndTime());
        tsc.fullName = pathname.getPathname();
        tsc.fileName = _computeOptions.getDssFilename();
        boolean exist = DssFileManagerImpl.getDssFileManager().exists(forecastDSS);
        if (exist) {
            if (!_computeOptions.shouldForceCompute()) {
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
//                I believe this if block is only initiated for output locations.
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
        for (DataLocation dl : dataLocations) {
            int i = dataLocations.indexOf(dl);
            if (!_dataLocations.contains(dl)) {
                DataLocation linkedTo = dl.getLinkedToLocation();
                String dssPath = linkedTo.getDssPath();
                if (validLinkedToDssPath(dl)) {
                    setModified(true);
                    setDssParts(dl);
                    _dataLocations.set(i, dl);
                    retval = true;
                }
            } else {
                DataLocation linkedTo = dl.getLinkedToLocation();
                String dssPath = linkedTo.getDssPath();
                if (validLinkedToDssPath(dl)) {
                    setModified(true);
                    setDssParts(dl);
                    retval = true;
                }
            }
        }
        if (retval) { saveData();}
        return retval;
    }
}
