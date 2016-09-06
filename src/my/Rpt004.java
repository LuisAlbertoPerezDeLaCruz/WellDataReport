/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import static java.lang.Double.parseDouble;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import miLibreria.ComboItem;
import static miLibreria.GlobalConstants.valorNulo;
import static miLibreria.GlobalConstants.valorNuloMuestra;
import miLibreria.ManejoDeCombos;
import static miLibreria.ManejoDeCombos.modeloCombo;
import miLibreria.bd.Clientes;
import miLibreria.bd.Diameters;
import miLibreria.bd.DrillingSubSectionType;
import miLibreria.bd.ManejoBDAccess;
import miLibreria.bd.ManejoBDI;
import miLibreria.bd.WellPlanPerMD;
import my.loadWellData.CargaWellPlan;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import miLibreria.xl.ManejoExcel;
import miLibreria.xl.ManejoXLS;
import miLibreria.xl.ManejoXLSX;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;



/**
 *
 * @author USUARIO
 */
public class Rpt004 extends javax.swing.JDialog {

    public long clienteId=0,campoId=0;
    public ManejoBDI oBD;
    private ManejoDeCombos oManejoDeCombos; 
    private DefaultListModel lmMacollas;
    private final javax.swing.JComboBox jComboBoxSubSections = new javax.swing.JComboBox();
    private CargaWellPlan oCAE=new CargaWellPlan(new javax.swing.JFrame(), true);
    private static DefaultTableModel modeloResultados;
    private DescriptiveStatistics oStats = new DescriptiveStatistics();
    private double angle=0;
    private long diameterId=0;
    private String archivoExcel="";
    private DecimalFormat df = new DecimalFormat("#0.000");

    
    public Rpt004(java.awt.Frame parent, boolean modal,ManejoBDI o ) {
        super(parent, modal);
        String s="";
        initComponents();

        for (int i=0;i<=this.jTableResultados.getColumnCount()-1;i++)
            this.jTableResultados.getColumnModel().getColumn(i).setPreferredWidth(100);
        this.jTableResultados.getColumnModel().getColumn(10).setPreferredWidth(200);
        this.jTableResultados.getColumnModel().getColumn(22).setPreferredWidth(150);

        setearTimer();
        this.jTableResultados.setDefaultRenderer (Object.class, new MiRender());
        this.jRadioButtonMotor.setSelected(true);
        modeloResultados = (DefaultTableModel) this.jTableResultados.getModel();
        oBD=o;
        oManejoDeCombos = new ManejoDeCombos(); 
        oManejoDeCombos.llenaCombo(oBD,oManejoDeCombos.getModeloCombo(),Clientes.class,this.jComboBoxClientes,"Select Client"); 
        oManejoDeCombos.llenaCombo(oBD,oManejoDeCombos.getModeloCombo(),Diameters.class,this.jComboBoxOD,"OD"); 
        s="SELECT DISTINCT TipoMotor.bendHousingAngle\n" +
          "FROM TipoMotor\n" +
          "ORDER BY TipoMotor.bendHousingAngle;";
        oManejoDeCombos.llenaCombo(oBD,oManejoDeCombos.getModeloCombo(),s,this.jComboBoxAngle,"BH Angle"); 
        lmMacollas=new DefaultListModel();
        this.jListMacollas.setModel(lmMacollas);        
        
        this.jTableSubSections.getColumnModel().getColumn(0).setCellRenderer(new ColorColumnRenderer(Color.LIGHT_GRAY, Color.blue));
        this.jTableSubSections.getColumnModel().getColumn(1).setPreferredWidth(200);
        this.jTableSubSections.getColumnModel().getColumn(1).setCellRenderer(new ColorColumnRenderer(Color.LIGHT_GRAY, Color.blue));
        this.jTableSubSections.getColumnModel().getColumn(2).setCellRenderer(new ColorColumnRenderer(Color.WHITE, Color.blue));
        this.jTableSubSections.getColumnModel().getColumn(3).setCellRenderer(new ColorColumnRenderer(Color.WHITE, Color.blue));

        for (int i=0;i<=this.jTableSubSections.getRowCount()-1;i++) {
            for (int j=0;j<=this.jTableSubSections.getColumnCount()-1;j++) {
                jTableSubSections.setValueAt("", i, j);                
            }
        } 

        oManejoDeCombos.llenaCombo(oBD,modeloCombo,DrillingSubSectionType.class,this.jComboBoxSubSections,"");
        jComboBoxSubSections.setEnabled(true);
        jComboBoxSubSections.setVisible(true);
        this.jTableSubSections.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(jComboBoxSubSections));
        

        this.jTableSubSections.getModel().addTableModelListener(
        new TableModelListener() 
        {
            int l=0;
            int row=0;
            int col=0;
            Object o;
            int rowCount=jTableSubSections.getRowCount();
            int colCount=jTableSubSections.getColumnCount();
            Object[][] a=new Object[rowCount][colCount];
            boolean cambiando=false;
            int j=0;
            int colCombo=1;
            int colPos=0;

            @Override
            public void tableChanged(TableModelEvent evt) 
            {                

                if (cambiando) return;


                for (int i=0;i<=rowCount-1;i++) {
                    for (int j=0;j<=colCount-1;j++) {
                        a[i][j]="";
                    }
                }

                l=0;

                row=evt.getFirstRow();
                col=evt.getColumn();
                ComboItem oCI=null;

                if (col==colCombo) {
                    for (int i=0;i<=rowCount-1;i++) {
                        o=jTableSubSections.getValueAt(i, colCombo);
                        if (o.getClass()==ComboItem.class){
                            oCI=(ComboItem) o;                        
                            if (oCI.getKey() !="" && oCI.getKey() !=null) {
                                try {
                                    for (j=0;j<=colCount-1;j++) {
                                        a[l][j]=jTableSubSections.getValueAt(i, j);
                                    }
                                    a[l][colPos]=""+(l+1);
                                    l++;
                                }
                                catch (NullPointerException ex){a[l][j]="";} 
                            } 
                        }
                    }

                    for (int i=0;i<rowCount;i++) {
                        for (int j=0;j<=colCount-1;j++) {
                            cambiando=true;
                            jTableSubSections.setValueAt(a[i][j], i, j);
                        }
                    }
                    cambiando=false;
                }                
            }
        });
        oCAE=new CargaWellPlan(parent,true);
        oCAE.setLocationRelativeTo(this);
    }
    
    public final void setearTimer() {
        int tiempoEnMilisegundos = 1000;
        Timer timer = new Timer (tiempoEnMilisegundos, new ActionListener () { 
        public void actionPerformed(ActionEvent e) {            
            boolean ok=true;
            int cantFilasLlenas=0;
            double desde=0,hasta=0;
            if (campoId<=0 || clienteId<=0 || diameterId<=0) ok=false;
            if (modeloResultados.getRowCount()==0) ok=false;
            if (jRadioButtonMotor.isSelected() && angle==0) ok=false;
            if (jListMacollas.getSelectionModel().isSelectionEmpty()) ok=false;
            for (int i=0;i<=jTableSubSections.getRowCount()-1;i++) {
                if (jTableSubSections.getValueAt(i, 1).toString().trim()!="") {
                    try {
                        if (!jTableSubSections.getValueAt(i, 2).toString().trim().replace(",",".").isEmpty())
                            desde=newDouble(jTableSubSections.getValueAt(i, 2).toString().trim().replace(",","."));
                        if (!jTableSubSections.getValueAt(i, 3).toString().trim().replace(",",".").isEmpty())
                            hasta=newDouble(jTableSubSections.getValueAt(i, 3).toString().trim().replace(",","."));                        
                    } catch (Exception ex) {
                        ok=false;
                    }
                    cantFilasLlenas++;
                }
                if ((hasta-desde)<=0) ok=false;
            }
            if (cantFilasLlenas==0) ok=false;
            jButtonProcesar.setEnabled(ok);
            pack();
        } 
        });
        timer.start();
    }

    
    private void cargaWellPlan() {
        oCAE.setVieneDelRpt004(true);
        oCAE.setParam(oBD);
        oCAE.setVisible(true);
        for (int i=modeloResultados.getRowCount()-1;i>=0;i--)
            modeloResultados.removeRow(i);
        mostrarWellPlan(oCAE.getWellPlanId(),false);
        archivoExcel=oCAE.jTextAreaArchivo.getText().trim();
        archivoExcel=oCAE.selectedFile.getAbsolutePath();
        int i=0;
    }
    
    public void mostrarWellPlan(long wellPlanId, boolean cadaCien) {
        ResultSet rs;
        int cant=0;
        WellPlanPerMD oWellPlanPerMD=new WellPlanPerMD();
        DecimalFormat df = new DecimalFormat("##########0.000");
        
        rs=oBD.select("SELECT count(*) FROM WellPlanPerMD WHERE wellPlanId="+wellPlanId);
        if (rs!=null) {
            try {
                while (rs.next()) {
                    cant=rs.getInt(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(Rpt004.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (cant>0) {
            Object[] o = new Object[cant];
            try {
                o=oBD.select(WellPlanPerMD.class, "wellPlanId="+wellPlanId);
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(Rpt004.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (int i=0;i<=cant-1;i+=(cadaCien)?100:1) {
                oWellPlanPerMD=(WellPlanPerMD) o[i];
                modeloResultados.addRow(new Object[] { oWellPlanPerMD.getMd(),
                preparaValor(df.format(oWellPlanPerMD.getIncl())),
                preparaValor(df.format(oWellPlanPerMD.getAzim())),
                preparaValor(df.format(oWellPlanPerMD.getTvd())),
                preparaValor(df.format(oWellPlanPerMD.getVsec())),
                preparaValor(df.format(oWellPlanPerMD.getNs())),
                preparaValor(df.format(oWellPlanPerMD.getEw())),
                preparaValor(df.format(oWellPlanPerMD.getDls())),
                oWellPlanPerMD.getTf() });
            }
        }
    }
    
    private Object preparaValor(Object o) {
        if (o.toString().contains(""+valorNulo)) {
            return valorNuloMuestra;
        } else
            return o;
    }
    
    private void procesar() {
        cursorEspera();
        Calculos oCalc;
        oCalc=new Calculos(oBD);
        ResultSet rs=null;
        ItemTable oi;
        Long[] aMacolla=new Long[lmMacollas.size()];
        boolean esRss=false,esMotor=false;
        PuntosDeMedicion oPM;
        PuntosDeMedicion[] aPM;
        ManejoExcel oXL=null;
        ManejoXLS oXLS=new ManejoXLS();
        ManejoXLSX oXLSX=new ManejoXLSX();
        String t;
        String composicion="";
        Double porcentajeArena=0.0;
        
        Double cantWork=0.0,sumPorArenaWork=0.0;
        
        for (int i=0;i<=modeloResultados.getRowCount()-1;i++) {
            for (int j=9;j<=modeloResultados.getColumnCount()-1;j++) {
                modeloResultados.setValueAt(" ", i, j);
            }
        }

        String filename = archivoExcel;        
        File selectedFile=new File(filename);
 
        t=filename.substring(filename.indexOf('.')+1).toUpperCase();
        if ("XLS".equals(t)) {
            oXL=oXLS;
        }
        if ("XLSX".equals(t)) {
            oXL=oXLSX;
        }
        try {
            oXL.setArchivo(selectedFile);
        } catch (NullPointerException ex){}
        
        if (this.jRadioButtonMotor.isSelected()) { 
            esMotor=true;            
        }
        else esRss=true;
        for (int i=0;i<=lmMacollas.size()-1;i++) {
            if (this.jListMacollas.isSelectedIndex(i)){
                oi=new ItemTable();
                oi=(ItemTable) lmMacollas.get(i);
                aMacolla[i]=oi.id;                
            }
        }
        double desde=0.0, hasta=0.0;
        String tipo="";
        int cantFilasSubSections=0;
        for (int i=0;i<=this.jTableSubSections.getRowCount()-1;i++) {
            if (this.jTableSubSections.getValueAt(i, 0)=="") {
                cantFilasSubSections=i;
                break;
            }          
        }
        
        //Encuentro los registros surveyPerMd que cumplen con la criteria
        String s="";
        long prevWellId=0, currWellId=0;
        boolean primerRegistro=true;
        s="SELECT CampoCliente.campoId, Well.macollaId, Well.id as wellId, Sections.diameterId, Diameters.size, Run.Id, BHA.id, DirectionalTool.motor, DirectionalTool.rss, TipoMotor.bendHousingAngle, RunSubSection.id, RunSubSection.drillingSubSectionId, DrillingSubSectionType.description, Survey.id, SurveyPerMD.md, SurveyPerMD.tvd, SurveyPerMD.dls\n" +
          "FROM (Macolla INNER JOIN (SurveyPerMD INNER JOIN (Survey INNER JOIN (DrillingSubSectionType INNER JOIN (RunSubSection INNER JOIN (Diameters INNER JOIN ((DirectionalTool INNER JOIN (BHA INNER JOIN (Run INNER JOIN (Sections INNER JOIN Well ON Sections.wellId = Well.id) ON Run.sectionId = Sections.id) ON BHA.runId = Run.Id) ON DirectionalTool.bhaId = BHA.id) LEFT JOIN TipoMotor ON DirectionalTool.tipoMotorId = TipoMotor.id) ON Diameters.id = Sections.diameterId) ON RunSubSection.runID = Run.Id) ON DrillingSubSectionType.id = RunSubSection.drillingSubSectionId) ON Survey.runId = Run.Id) ON SurveyPerMD.surveyId = Survey.id) ON Macolla.id = Well.macollaId) INNER JOIN CampoCliente ON Macolla.campoClienteId = CampoCliente.id\n" +
          "WHERE CampoCliente.campoId="+campoId;
               
        s+=" AND SurveyPerMD.md>=[runsubsection].[profundidadinicial] And (SurveyPerMD.md)<=[runsubsection].[profundidadFinal] " ;

        s+=" ORDER BY Well.id, description,tvd asc;";
        
        rs=oBD.select(s);
        int cant=0;
        try {
            while (rs.next()) {
                cant++;
            }
        } catch (Exception ex) {
            Logger.getLogger(Rpt004.class.getName()).log(Level.SEVERE, null, ex);
            msgbox("No se encuentran registros con esta criteria.","Error");
            cursorNormal();
            return;
        }
        
        //Dimensiono y lleno array de PuntosDeMedicion con calculos requeridos
        aPM=new PuntosDeMedicion[cant];
        double tvd=0, md=0, porcentajeSliding=0, promedioFlow=0;
        double promedioWob=0, promedioDifferential=0,promedioSrpm=0, promedioTfAngle=0;
        String descripcion="";
        int p=0;
        long macollaId=0;
        boolean macollaSi=false;
        
        try {
            rs.beforeFirst();
            proxReg:
            while (rs.next()){
                macollaSi=false;
                macollaId=rs.getLong("macollaId");
                for (int m=0;m<=lmMacollas.getSize()-1;m++) {
                    if (this.jListMacollas.isSelectedIndex(m)) {
                        oi=(ItemTable) lmMacollas.getElementAt(m);
                        if (oi.id==macollaId) macollaSi=true;                        
                    }
                }
                if (!macollaSi) {
                    continue;
                }
                oPM=new PuntosDeMedicion();
                if (primerRegistro) {
                    primerRegistro=false;
                    currWellId=rs.getLong("wellId");
                    prevWellId=currWellId;
                    oCalc.cargarPozo(currWellId);
                } else {
                  prevWellId=currWellId;
                  currWellId=rs.getLong("wellId");  
                }
                if (prevWellId!=currWellId) {
                   oCalc.cargarPozo(currWellId); 
                }
                tvd=rs.getDouble("tvd");
                descripcion=rs.getString("description").trim();
                md=rs.getDouble("md");
                
                if (esRss) 
                    porcentajeSliding=oCalc.getPorcentajeSteering(md);
                else
                    porcentajeSliding=oCalc.getPorcentajeSliding(md);
                promedioFlow=oCalc.getPromedioFlow(md);
                promedioWob=oCalc.getPromedioWob(md);
                promedioDifferential=oCalc.getPromedioDifferential(md);
                promedioSrpm=oCalc.getPromedioSrpmRotating(md);
                promedioTfAngle=oCalc.getPromedioTfAngleSliding(md);
                porcentajeArena=oCalc.getPorcentajeArena(md);
                if (porcentajeArena==0) {
                    porcentajeArena += 0.000001;
                }

                if (porcentajeArena>=80) {
                    composicion="Sand";
                } else
                {
                    if (porcentajeArena>=50) {
                        composicion="Shale-Sand intercalation";
                    } else
                        composicion="Shale";
                }                
                
                if (porcentajeSliding==0) continue;
                
                for (int i=0;i<=cantFilasSubSections-1;i++){
                    tipo=this.jTableSubSections.getValueAt(i, 1).toString().trim();
                    desde=(Double) this.jTableSubSections.getValueAt(i, 2);
                    hasta=(Double) this.jTableSubSections.getValueAt(i, 3); 
                    if (tvd>=desde && tvd<=hasta) {                           
                        oPM.setDls(rs.getDouble("dls"));
                        oPM.setPorcentajeSliding(porcentajeSliding);
                        oPM.setTipo(descripcion);
                        oPM.setTvd(tvd);
                        oPM.setDiameterId(diameterId);
                        oPM.setMotor(esMotor);
                        oPM.setRss(esRss);
                        oPM.setBenHousingAngle(angle);
                        oPM.setPorcentajeArena(porcentajeArena);
                        oPM.setPromedioFlow(promedioFlow);
                        oPM.setPromedioWob(promedioWob);
                        oPM.setPromedioDifferential(promedioDifferential);
                        oPM.setPromedioSrpm(promedioSrpm);
                        oPM.setPromedioTfAngle(promedioTfAngle);
                        aPM[p]=oPM;
                        p++;
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Rpt004.class.getName()).log(Level.SEVERE, null, ex);
        }
        int cantRegEnArrayPM=p;
        
        //Sumarizo los PuntosDeMedicion cada cien pies en array de ItemSegmento        
        //primero encuentro la dimension del array y lo dimensiono
        ItemSegmento[][] aISsum;
        long cantMaximaSegmentos=0;
        ItemSegmento oIS;

        cantMaximaSegmentos=modeloResultados.getRowCount();
        aISsum=new ItemSegmento[cantFilasSubSections][(int)cantMaximaSegmentos];
        
        //Inicializo el array de segmentos
        for (int i=0;i<=cantFilasSubSections-1;i++){
            for (int j=0;j<=cantMaximaSegmentos-1;j++) {
                oIS=new ItemSegmento();
                aISsum[i][j]=oIS;
            }
        }
        
        //Ahora segmento segun intervalos de los TVD del wellPlan
        
        for (int i=0;i<=aISsum.length-1;i++) {  // Nivel tipo en pantalla
            tipo=this.jTableSubSections.getValueAt(i, 1).toString();
            desde=(Double) this.jTableSubSections.getValueAt(i, 2);
            hasta=(Double) this.jTableSubSections.getValueAt(i, 3);
            double prevd=desde;
            int p1=0;
            //aqui segmento
            Double tvdIn=0.0;
            boolean listo=false;
            int seg=0;
            for (int wpi=0;wpi<=modeloResultados.getRowCount()-1;wpi++) { // Nivel lineas del plan
                tvdIn=newDouble(modeloResultados.getValueAt(wpi, 3).toString());
                if (tvdIn>=desde) {
                    for (seg=wpi;seg<=modeloResultados.getRowCount()-1;seg++) {
                        tvdIn=newDouble(modeloResultados.getValueAt(seg, 3).toString());
                        if (tvdIn>hasta) {
                            listo=true;
                            break;
                        }
                        try {
                            aISsum[i][p1].setTipo(tipo);
                            aISsum[i][p1].setTvdDesde(prevd+0.01);
                            aISsum[i][p1].setTvdHasta(tvdIn);
                            aISsum[i][p1].setSumatoriaDls(0);
                            aISsum[i][p1].setSumatoriaPorcentajeSliding(0);
                            aISsum[i][p1].setSumatoriaPromediosFlow(0);
                            aISsum[i][p1].setSumatoriaPromediosWob(0);
                            aISsum[i][p1].setSumatoriaPromediosDifferential(0);
                            aISsum[i][p1].setSumatoriaPromediosSrpm(0);
                            aISsum[i][p1].setSumatoriaPromediosTfAngle(0);
                            prevd=tvdIn;
                            p1++;
                        } catch (ArrayIndexOutOfBoundsException ex) {}
                    }
                }
                if (listo || seg>=modeloResultados.getRowCount()-1) break;
            }
 
            //aqui sumarizo
            Double dlsAl100PorCiento=0.0;
            int j,k;
            for (j=0;j<=cantRegEnArrayPM-1;j++) {   //Entradas registradas de Puntos de Medicion
                for (k=0;k<=aISsum[i].length-1;k++) { //Nivel Lineas del Plan
                    if (aPM[j].getTvd()>aISsum[i][k].getTvdDesde() && aPM[j].getTvd()<=aISsum[i][k].getTvdHasta()) {
                       if (aPM[j].getPorcentajeArena()>0.001)
                           aISsum[i][k].addStats2Value(aPM[j].getPorcentajeArena());
                       if (pasaCriteria(aPM[j],tipo,diameterId,angle,esMotor,esRss)){
                            aISsum[i][k].setSumatoriaDls(aISsum[i][k].getSumatoriaDls()+aPM[j].getDls());                          
                            aISsum[i][k].setSumatoriaPorcentajeSliding(aISsum[i][k].getSumatoriaPorcentajeSliding()+aPM[j].getPorcentajeSliding());
                            aISsum[i][k].setN(aISsum[i][k].getN()+1);
                            dlsAl100PorCiento=(aPM[j].getPorcentajeSliding()>0)?aPM[j].getDls()*100/aPM[j].getPorcentajeSliding():0;
                            aISsum[i][k].addStatsValue(dlsAl100PorCiento);
                            if (aPM[j].getPromedioFlow()>0)
                                aISsum[i][k].addStats3Value(aPM[j].getPromedioFlow());                           
                            if (aPM[j].getPromedioWob()>0)
                                aISsum[i][k].addStats4Value(aPM[j].getPromedioWob());                           
                            if (aPM[j].getPromedioDifferential()>0)
                                aISsum[i][k].addStats5Value(aPM[j].getPromedioDifferential());                           
                            if (aPM[j].getPromedioSrpm()>0)
                                aISsum[i][k].addStats6Value(aPM[j].getPromedioSrpm()); 
                            //No lo limito ya que existen angulos negativos (validos)
                            if (aPM[j].getPromedioTfAngle()!=0)
                                aISsum[i][k].addStats7Value(aPM[j].getPromedioTfAngle());
                       }                           
                    }
                }
            }            
        }
        
        final int posMD=0,
                  posIncl=1,
                  posAzim=2,
                  posTVD=3,
                  posVsec=4,
                  posNs=5,
                  posEw=6,
                  posDLS=7, 
                  posTF=8, 
                  posSubSection=9, 
                  posDLS100=10,
                  posN1=11,
                  posS1=12,
                  posRisk=13,
                  posArena=14,
                  posN2=15,
                  posS2=16,
                  posComposicion=17,
                  posFlow=18,
                  posWob=19,
                  posDifferential=20,
                  posSrpm=21,
                  posTfAngle=22;
        
        //Cargo en pantalla los resultados de la sumarización
        Double wpTvd=0.0, wpDls=0.0;
        double desv=0.0,mean=0.0,risk=0.0;
        long n=0;
        boolean encontrado=false;
        
        for (int wpi=0;wpi<=modeloResultados.getRowCount()-1;wpi++) { //Nivel Lineas del Plan
            wpTvd=newDouble(modeloResultados.getValueAt(wpi, posTVD).toString());
            wpDls=newDouble(modeloResultados.getValueAt(wpi, posDLS).toString());
            encontrado=false;
            for (int i=0;i<=aISsum.length-1;i++) { //Nivel del Tipo en Pantalla
                for (int j=0;j<=aISsum[i].length-1;j++){ 
                    if ("".equals(aISsum[i][j].getTipo())){
                        break;
                    }
                    if (wpTvd>=aISsum[i][j].getTvdDesde() && wpTvd<=aISsum[i][j].getTvdHasta()) {
                        tipo=aISsum[i][j].getTipo();
                        desv=aISsum[i][j].statsStandardDeviation();
                        n=(Long)aISsum[i][j].statsN();                        
                        if (aISsum[i][j].getSumatoriaPorcentajeSliding()>0)
                            mean=aISsum[i][j].getSumatoriaDls()*100/aISsum[i][j].getSumatoriaPorcentajeSliding();
                        else mean=0;
                                               
                        modeloResultados.setValueAt(tipo ,wpi, posSubSection);
                        modeloResultados.setValueAt((mean>=0)?mean:"" ,wpi, posDLS100);
                        modeloResultados.setValueAt((n>=0)?n:"" ,wpi, posN1);
                        modeloResultados.setValueAt((desv>=0)?desv:"" ,wpi, posS1);
                        if (mean>0) {
                            risk=(wpDls/mean)*100;
                            modeloResultados.setValueAt((risk>=0)?risk:"" ,wpi, posRisk);
                        } else
                            modeloResultados.setValueAt("" ,wpi, posRisk);
           
                        modeloResultados.setValueAt((aISsum[i][j].stats2Media()>=0)?aISsum[i][j].stats2Media():"" ,wpi, posArena);
                        modeloResultados.setValueAt(aISsum[i][j].stats2N() ,wpi, posN2);
                        if (aISsum[i][j].stats2N()>0)
                            modeloResultados.setValueAt(aISsum[i][j].stats2StandardDeviation() ,wpi, posS2);
                        

                        if (aISsum[i][j].stats2Media()>=80) {
                            modeloResultados.setValueAt( "Sand",wpi, posComposicion);
                        } else
                        {
                            if (aISsum[i][j].stats2Media()>=50) {
                                modeloResultados.setValueAt( "Shale-Sand intercalation",wpi, posComposicion);
                            } else
                                if (aISsum[i][j].stats2N()>0)
                                    modeloResultados.setValueAt( "Shale",wpi, posComposicion);
                        }
                        modeloResultados.setValueAt((aISsum[i][j].stats3Media()>=0)?aISsum[i][j].stats3Media():"" ,wpi, posFlow);
                        modeloResultados.setValueAt((aISsum[i][j].stats4Media()>=0)?aISsum[i][j].stats4Media():"" ,wpi, posWob);
                        modeloResultados.setValueAt((aISsum[i][j].stats5Media()>=0)?aISsum[i][j].stats5Media():"" ,wpi, posDifferential);
                        modeloResultados.setValueAt((aISsum[i][j].stats6Media()>=0)?aISsum[i][j].stats6Media():"" ,wpi, posSrpm);
                        if (Double.isNaN(aISsum[i][j].stats7Media())==false)
                            modeloResultados.setValueAt(aISsum[i][j].stats7Media() ,wpi, posTfAngle);
                        encontrado=true;
                        break;
                    }
                }
                if (encontrado) break;
            } 
        }
 
        //Realizo Interpolacion donde el dls al 100% es cero
        //Reviso y completo datos vacios
        double prevmd=0.0,prevmean=0.0,prevrisk=0.0,nextmean,nextrisk=0.0,nextmd=0.0;
        for (int wpi=0;wpi<=modeloResultados.getRowCount()-1;wpi++) {
            if (modeloResultados.getValueAt(wpi, posSubSection)==null) continue;
            tipo=modeloResultados.getValueAt(wpi, posSubSection).toString().trim();
            if ("".equals(tipo)) continue; 
            wpDls=newDouble(modeloResultados.getValueAt(wpi, posDLS).toString().trim());
            md=newDouble(modeloResultados.getValueAt(wpi, posMD).toString().trim());
            mean=newDouble(modeloResultados.getValueAt(wpi, posDLS100).toString());
            if ("".equals(modeloResultados.getValueAt(wpi, posRisk).toString().trim())) 
                risk=0.0;
            else risk=newDouble(modeloResultados.getValueAt(wpi, posRisk).toString().trim());
            if (mean==0) { //reviso a ver si hay otra fila con valor
                int i=wpi;
                nextmean=0.0;
                while(!"".equals(tipo)) {
                    if (i<modeloResultados.getRowCount()-2) {
                        i++;
                        if (modeloResultados.getValueAt(i, posSubSection)==null) continue;
                        tipo=modeloResultados.getValueAt(i, posSubSection).toString().trim();
                        nextmd=newDouble(modeloResultados.getValueAt(i, posMD).toString().trim());
                        if ("".equals(modeloResultados.getValueAt(i, posDLS100).toString().trim()))
                            nextmean=0;
                        else nextmean=newDouble(modeloResultados.getValueAt(i, posDLS100).toString());
                        if ("".equals(modeloResultados.getValueAt(i, posRisk).toString().trim())) 
                            risk=0.0;
                        else nextrisk=newDouble(modeloResultados.getValueAt(i, posRisk).toString().trim());
                        if (nextmean>0) break;
                    } else break;
                }
                if (nextmean==0) { //caso donde no hay mas mean adelante
                   if (prevmean>=0) {
                       modeloResultados.setValueAt(prevmean ,wpi, posDLS100);
                       modeloResultados.setValueAt("0" ,wpi, posN1);
                       modeloResultados.setValueAt("0.0" ,wpi, posS1);
                       modeloResultados.setValueAt(prevrisk ,wpi, posRisk);
                   }
                }
                if (nextmean>0) { //caso donde aparece el primer mean y todos los anteriores son cero
                   if (prevmean==0) {
                       modeloResultados.setValueAt(nextmean ,wpi, posDLS100);
                       modeloResultados.setValueAt("0" ,wpi, posN1);
                       modeloResultados.setValueAt("0.0" ,wpi, posS1);
                       modeloResultados.setValueAt(nextrisk ,wpi, posRisk);
                   }                    
                }
                double m=0.0;
                double workmean=0.0;
                if (nextmean>0) { //caso donde hay que prorratear punto a punto con la ecuacion de la recta
                   if (prevmean>0) {
                       m=(nextmean-prevmean)/(nextmd-prevmd);
                       workmean=m*(md-prevmd)+prevmean;                       
                       modeloResultados.setValueAt(workmean ,wpi, posDLS100);
                       modeloResultados.setValueAt("0" ,wpi, posN1);
                       modeloResultados.setValueAt("0.0" ,wpi, posS1);
                       if (workmean>0) {
                           risk=(wpDls/workmean)*100;
                           modeloResultados.setValueAt((risk>=0)?risk:"" ,wpi, posRisk);
                       } else
                            modeloResultados.setValueAt("" ,wpi, posRisk);
                   }
                }
                
            } else {
                prevmean=mean;
                prevrisk=risk;
                prevmd=md;
            }
                
        }

        if (oXL.abrirArchivo()) {
            int i=0; 
            Double value=0.0;
            oXL.establecerHoja(0);
            CellStyle styleStr=oXL.getCellStyle(22, 1);
            styleStr.setBorderBottom(XSSFCellStyle.BORDER_NONE);
            CellStyle styleDbl=oXL.getCellStyle(23, 1);
            oXL.setValorCelda(22, 14, "tf");            
            oXL.setValorCelda(22, 15, "SubSeccion\ncomposicion");
            oXL.setValorCelda(22, 16, "dls 100\n(%)");
            oXL.setValorCelda(22, 17, "n\n");
            oXL.setValorCelda(22, 18, "s\n(ft)");
            oXL.setValorCelda(22, 19, "risk\n(%)");
            oXL.setValorCelda(22, 20, "arena\n(%)");
            oXL.setValorCelda(22, 21, "n\n");
            oXL.setValorCelda(22, 22, "s\n(ft)");
            oXL.setValorCelda(22, 23, "composicion\n");
            oXL.setValorCelda(22, 24, "flow\n");
            oXL.setValorCelda(22, 25, "wob\n");
            oXL.setValorCelda(22, 26, "differential\n");
            oXL.setValorCelda(22, 27, "srpm\n");
            oXL.setValorCelda(22, 28, "tfAngle\n");
            oXL.setCellStyle(22, 14, styleStr);            
            oXL.setCellStyle(22, 15, styleStr);
            oXL.setCellStyle(22, 16, styleStr);
            oXL.setCellStyle(22, 17, styleStr);
            oXL.setCellStyle(22, 18, styleStr);
            oXL.setCellStyle(22, 19, styleStr);
            oXL.setCellStyle(22, 20, styleStr);
            oXL.setCellStyle(22, 21, styleStr);
            oXL.setCellStyle(22, 22, styleStr);
            oXL.setCellStyle(22, 23, styleStr);    
            oXL.setCellStyle(22, 24, styleStr);
            oXL.setCellStyle(22, 25, styleStr);
            oXL.setCellStyle(22, 26, styleStr);
            oXL.setCellStyle(22, 27, styleStr);
            oXL.setCellStyle(22, 28, styleStr);
            int offset=24;
            String tvd_find="";
            for (i=0;i<=modeloResultados.getRowCount()-1;i++) {
                try {
                    tvd_find=modeloResultados.getValueAt(i, 3).toString();
                    for (int j=23;j<=oXL.getLimites()[0]-1;j++) {
                        try {
                            if ((newDouble(oXL.valorCelda(j, 4)) - newDouble(tvd_find))<=0.01) {
                                offset=j;
                            }
                        } catch (Exception ex){}
                    }
                    oXL.setValorCelda(offset, 14, modeloResultados.getValueAt(i, posTF));
                    oXL.setCellStyle(offset, 14, styleStr);                    
                    oXL.setValorCelda(offset, 15, modeloResultados.getValueAt(i, posSubSection));
                    oXL.setCellStyle(offset, 15, styleStr);
                    oXL.setValorCelda(offset, 16, modeloResultados.getValueAt(i, posDLS100));
                    oXL.setCellStyle(offset, 16, styleDbl);
                    oXL.setValorCelda(offset, 17, modeloResultados.getValueAt(i, posN1));
                    oXL.setCellStyle(offset, 17, styleDbl);
                    oXL.setValorCelda(offset, 18, modeloResultados.getValueAt(i, posS1));
                    oXL.setCellStyle(offset, 18, styleDbl);
                    oXL.setValorCelda(offset, 19, modeloResultados.getValueAt(i, posRisk));
                    oXL.setCellStyle(offset, 19, styleDbl);
                    value=newDouble(modeloResultados.getValueAt(i, posRisk).toString());
                    if (value>=100.0){
                       oXL.setCellColor(offset, 19, IndexedColors.GREY_80_PERCENT, IndexedColors.WHITE);       
                    } else {
                       if (value>=80.0){
                          oXL.setCellColor(offset, 19, IndexedColors.RED, IndexedColors.WHITE);
                       } else {
                          if ((Double)value>=50.0){
                             oXL.setCellColor(offset, 19, IndexedColors.YELLOW, IndexedColors.BLACK);
                          } else {
                             if ((Double)value>=0.0){
                                oXL.setCellColor(offset, 19, IndexedColors.GREEN, IndexedColors.WHITE);
                             } 
                          } 
                       }
                    }
                    oXL.setValorCelda(offset, 20, modeloResultados.getValueAt(i, posArena));
                    oXL.setCellStyle(offset, 20, styleDbl);
                    oXL.setValorCelda(offset, 21, modeloResultados.getValueAt(i, posN2));
                    oXL.setCellStyle(offset, 21, styleDbl);
                    oXL.setValorCelda(offset, 22, modeloResultados.getValueAt(i, posS2));
                    oXL.setCellStyle(offset, 22, styleDbl);   
                    oXL.setValorCelda(offset, 23, modeloResultados.getValueAt(i, posComposicion));
                    oXL.setCellStyle(offset, 23, styleStr);
                    String sValue=new String(modeloResultados.getValueAt(i, posComposicion).toString().trim());
                    if ("Sand".equals(sValue)) {
                       oXL.setCellColor(offset, 23, IndexedColors.YELLOW, IndexedColors.BLACK);                 
                    }
                    if ("Shale-Sand intercalation".equals(sValue)) {
                       oXL.setCellColor(offset, 23, IndexedColors.ORANGE, IndexedColors.WHITE);                 
                    }
                    if ("Shale".equals(sValue)) {
                       oXL.setCellColor(offset, 23, IndexedColors.GREY_80_PERCENT, IndexedColors.WHITE);                 
                    }
                    oXL.setValorCelda(offset, 24, modeloResultados.getValueAt(i, posFlow));
                    oXL.setCellStyle(offset, 24, styleStr); 
                    oXL.setValorCelda(offset, 25, modeloResultados.getValueAt(i, posWob));
                    oXL.setCellStyle(offset, 25, styleStr);
                    oXL.setValorCelda(offset, 26, modeloResultados.getValueAt(i, posDifferential));
                    oXL.setCellStyle(offset, 26, styleStr);
                    oXL.setValorCelda(offset, 27, modeloResultados.getValueAt(i, posSrpm));
                    oXL.setCellStyle(offset, 27, styleStr);
                    oXL.setValorCelda(offset, 28, modeloResultados.getValueAt(i, posTfAngle));
                    oXL.setCellStyle(offset, 28, styleStr);
                }catch (Exception ex){
                }
            }
            oXL.writeWorkBook();
        }
        cursorNormal();
    }
    
    boolean pasaCriteria(PuntosDeMedicion oPM, String tipo, long diameterId, double angle, boolean motor, boolean rss){
        boolean ok=false;
        if (oPM.getTipo().equals(tipo)) {
            if (oPM.getDiameterId()==diameterId) {
                if (oPM.getBenHousingAngle()==angle) {
                    if (motor == oPM.isMotor()) {
                        if (rss == oPM.isRss()) {
                            ok=true;
                        }
                    }
                }  
            }
        }
        return ok;
    }
    
    public void cursorNormal(){
        this.setCursor(Cursor.getDefaultCursor());
    }
    
    public void cursorEspera(){
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
    
    
    public void msgbox(String s, String t){
        JOptionPane.showMessageDialog(null, s,t,TrayIcon.MessageType.WARNING.ordinal());        
    }
    
    
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
    
    public static double newDouble(String s) {
        return parseDouble(s.replace(",", "."));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jComboBoxClientes = new javax.swing.JComboBox();
        jComboBoxCampo = new javax.swing.JComboBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListMacollas = new javax.swing.JList();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTableSubSections = new javax.swing.JTable();
        jComboBoxOD = new javax.swing.JComboBox();
        jButtonEscogerPlan = new javax.swing.JButton();
        jButtonProcesar = new javax.swing.JButton();
        jButtonSalir = new javax.swing.JButton();
        jRadioButtonMotor = new javax.swing.JRadioButton();
        jRadioButtonRSS = new javax.swing.JRadioButton();
        jComboBoxAngle = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableResultados = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Well Planning Risk Analysis");
        setMinimumSize(new java.awt.Dimension(910, 600));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jComboBoxClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxClientesActionPerformed(evt);
            }
        });
        getContentPane().add(jComboBoxClientes, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 270, -1));

        jComboBoxCampo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxCampoActionPerformed(evt);
            }
        });
        getContentPane().add(jComboBoxCampo, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 30, 210, -1));

        jScrollPane2.setViewportView(jListMacollas);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 30, 160, 160));

        jTableSubSections.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Number", "Sub-Section Type", "TVD from (ft)", "TVD to (ft)"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableSubSections.getTableHeader().setReorderingAllowed(false);
        jTableSubSections.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTableSubSectionsFocusLost(evt);
            }
        });
        jTableSubSections.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableSubSectionsMouseClicked(evt);
            }
        });
        jTableSubSections.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                jTableSubSectionsInputMethodTextChanged(evt);
            }
        });
        jTableSubSections.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTableSubSectionsKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTableSubSectionsKeyReleased(evt);
            }
        });
        jScrollPane3.setViewportView(jTableSubSections);

        getContentPane().add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 510, 120));

        jComboBoxOD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxODActionPerformed(evt);
            }
        });
        getContentPane().add(jComboBoxOD, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 50, 120, -1));

        jButtonEscogerPlan.setText("Import well plan");
        jButtonEscogerPlan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEscogerPlanActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonEscogerPlan, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 520, -1, -1));

        jButtonProcesar.setText("Process");
        jButtonProcesar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonProcesarActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonProcesar, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 520, -1, -1));

        jButtonSalir.setText("Exit");
        jButtonSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSalirActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonSalir, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 520, 80, -1));

        buttonGroup1.add(jRadioButtonMotor);
        jRadioButtonMotor.setText("Motor");
        jRadioButtonMotor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMotorActionPerformed(evt);
            }
        });
        getContentPane().add(jRadioButtonMotor, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 150, -1, -1));

        buttonGroup1.add(jRadioButtonRSS);
        jRadioButtonRSS.setText("RSS");
        jRadioButtonRSS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonRSSActionPerformed(evt);
            }
        });
        getContentPane().add(jRadioButtonRSS, new org.netbeans.lib.awtextra.AbsoluteConstraints(820, 150, -1, -1));

        jComboBoxAngle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxAngleActionPerformed(evt);
            }
        });
        getContentPane().add(jComboBoxAngle, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 100, 120, -1));

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setAutoscrolls(true);

        jTableResultados.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "MD", "Incl (deg) ", "AZIM (deg)", "TVD (ft) ", "VSEC (ft)", "NS (ft)", "EW (ft)", "DLS (°/100ft) ", "TF Proposed (deg)", "SubSection ", "DLS extrapolated 100% (°/100ft)", "n", "s", " Risk 100 %", "Sand % ", "n", "s", "Formation", "Flow (gal/min)", "WOB (1000 lbf)", "Differential (psi)", "SRPM (c/min)", "TF Angle Average (deg)"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableResultados.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane1.setViewportView(jTableResultados);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, 870, 300));

        jLabel1.setText("Pads:");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 10, 60, -1));

        jLabel2.setText("Hole Size (OD in):");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 30, 120, -1));

        jLabel3.setText("Bend housing angle (deg):");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 80, 140, -1));

        jLabel4.setText("Directional Tool:");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 130, -1, -1));

        jLabel5.setText("Client:");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 60, -1));

        jLabel6.setText("Field:");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 10, 50, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxClientesActionPerformed
        clienteId=oManejoDeCombos.getComboID(this.jComboBoxClientes);
        String s="SELECT campoId,campoNombre from ConsultaCampoCliente1 WHERE clienteId="+clienteId;
        oManejoDeCombos.llenaCombo(oBD,oManejoDeCombos.getModeloCombo(),s,this.jComboBoxCampo,"Select Field");
    }//GEN-LAST:event_jComboBoxClientesActionPerformed

    private void jComboBoxCampoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxCampoActionPerformed
        campoId=oManejoDeCombos.getComboID(this.jComboBoxCampo);
        String s="SELECT macollaId,macollaNombre from ConsultaMacolla1 WHERE clienteId="+clienteId + " AND campoId="+campoId;
        ItemTable oi;
        ResultSet rs=oBD.select(s);
        if (lmMacollas!=null)
            lmMacollas.clear();
        try {
            while(rs.next()) {
                oi=new ItemTable();
                oi.id=rs.getLong("macollaId");
                oi.nombre=rs.getString("macollaNombre");
                lmMacollas.addElement(oi);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Rpt004.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_jComboBoxCampoActionPerformed

    private void jTableSubSectionsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTableSubSectionsFocusLost

    }//GEN-LAST:event_jTableSubSectionsFocusLost

    private void jTableSubSectionsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableSubSectionsMouseClicked

    }//GEN-LAST:event_jTableSubSectionsMouseClicked

    private void jTableSubSectionsInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_jTableSubSectionsInputMethodTextChanged

    }//GEN-LAST:event_jTableSubSectionsInputMethodTextChanged

    private void jTableSubSectionsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTableSubSectionsKeyPressed

    }//GEN-LAST:event_jTableSubSectionsKeyPressed

    private void jTableSubSectionsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTableSubSectionsKeyReleased

    }//GEN-LAST:event_jTableSubSectionsKeyReleased

    private void jComboBoxODActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxODActionPerformed
        diameterId=this.oManejoDeCombos.getComboID(jComboBoxOD);
    }//GEN-LAST:event_jComboBoxODActionPerformed

    private void jButtonEscogerPlanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEscogerPlanActionPerformed
        cargaWellPlan();
    }//GEN-LAST:event_jButtonEscogerPlanActionPerformed

    private void jComboBoxAngleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxAngleActionPerformed
        angle=0.0;
        if (this.oManejoDeCombos.getComboID(jComboBoxAngle)>0)
           angle=newDouble(this.jComboBoxAngle.getSelectedItem().toString());
    }//GEN-LAST:event_jComboBoxAngleActionPerformed

    private void jButtonProcesarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonProcesarActionPerformed
        procesar();
    }//GEN-LAST:event_jButtonProcesarActionPerformed

    private void jRadioButtonRSSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonRSSActionPerformed
        this.jComboBoxAngle.setSelectedIndex(0);
        this.jComboBoxAngle.setEnabled(false);
    }//GEN-LAST:event_jRadioButtonRSSActionPerformed

    private void jRadioButtonMotorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMotorActionPerformed
        this.jComboBoxAngle.setEnabled(true);
    }//GEN-LAST:event_jRadioButtonMotorActionPerformed

    private void jButtonSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSalirActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButtonSalirActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Rpt004.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Rpt004.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Rpt004.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Rpt004.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Rpt004 dialog = new Rpt004(new javax.swing.JFrame(), true,new ManejoBDAccess());
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonEscogerPlan;
    private javax.swing.JButton jButtonProcesar;
    private javax.swing.JButton jButtonSalir;
    private javax.swing.JComboBox jComboBoxAngle;
    private javax.swing.JComboBox jComboBoxCampo;
    private javax.swing.JComboBox jComboBoxClientes;
    private javax.swing.JComboBox jComboBoxOD;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JList jListMacollas;
    private javax.swing.JRadioButton jRadioButtonMotor;
    private javax.swing.JRadioButton jRadioButtonRSS;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTableResultados;
    private javax.swing.JTable jTableSubSections;
    // End of variables declaration//GEN-END:variables
}

class ItemTable {
    long id;
    String nombre;
    public String toString(){
        return nombre;
    }
}

class ColorColumnRenderer extends DefaultTableCellRenderer
{
   Color bkgndColor, fgndColor;
     
   public ColorColumnRenderer(Color bkgnd, Color foregnd) {
      super();
      bkgndColor = bkgnd;
      fgndColor = foregnd;
   }
       
   public Component getTableCellRendererComponent
        (JTable table, Object value, boolean isSelected,
         boolean hasFocus, int row, int column)
   {
      Component cell = super.getTableCellRendererComponent
         (table, value, isSelected, hasFocus, row, column);
  
      cell.setBackground( bkgndColor );
      cell.setForeground( fgndColor );
      
      return cell;
   }
}

class PuntosDeMedicion {
    private double tvd;
    private String tipo;
    private double dls;
    private boolean motor;
    private boolean rss;
    private long diameterId;
    private double benHousingAngle;
    private double porcentajeSliding;
    private double porcentajeArena;
    private double promedioFlow;
    private double promedioWob;
    private double promedioDifferential;
    private double promedioSrpm;
    private double promedioTfAngle;
    
    public PuntosDeMedicion(){
        tvd=0.0;
        tipo="";
        dls=0.0;
        motor=false;
        rss=false;
        diameterId=0;
        benHousingAngle=0.0;
        porcentajeSliding=0.0;
        porcentajeArena=0.0;
        promedioFlow=0.0;
        promedioWob=0.0;
        promedioDifferential=0.0;
        promedioSrpm=0.0;
        promedioTfAngle=0.0;
    }
    
    public double getPorcentajeArena() {
        return porcentajeArena;
    }
    
    public void setPorcentajeArena(double v) {
        porcentajeArena=v;
    }
    
    public double getPromedioFlow() {
        return promedioFlow;
    }
    
    public void setPromedioFlow(double v) {
        promedioFlow=v;
    }
    
    public double getTvd() {
        return tvd;
    }

    public void setTvd(double tvd) {
        this.tvd = tvd;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getDls() {
        return dls;
    }

    public void setDls(double dls) {
        this.dls = dls;
    }

    public double getPorcentajeSliding() {
        return porcentajeSliding;
    }

    public void setPorcentajeSliding(double porcentajeSliding) {
        this.porcentajeSliding = porcentajeSliding;
    }

    public double getPromedioWob() {
        return promedioWob;
    }

    public void setPromedioWob(double promedioWob) {
        this.promedioWob = promedioWob;
    }

    public double getPromedioDifferential() {
        return promedioDifferential;
    }

    public void setPromedioDifferential(double promedioDifferential) {
        this.promedioDifferential = promedioDifferential;
    }

    public double getPromedioSrpm() {
        return promedioSrpm;
    }

    public void setPromedioSrpm(double promedioSrpm) {
        this.promedioSrpm = promedioSrpm;
    }

    public double getPromedioTfAngle() {
        return promedioTfAngle;
    }

    public void setPromedioTfAngle(double promedioTfAngle) {
        this.promedioTfAngle = promedioTfAngle;
    }

    /**
     * @return the benHousingAngle
     */
    public double getBenHousingAngle() {
        return benHousingAngle;
    }

    /**
     * @param benHousingAngle the benHousingAngle to set
     */
    public void setBenHousingAngle(double benHousingAngle) {
        this.benHousingAngle = benHousingAngle;
    }

    /**
     * @return the motor
     */
    public boolean isMotor() {
        return motor;
    }

    /**
     * @param motor the motor to set
     */
    public void setMotor(boolean motor) {
        this.motor = motor;
    }

    /**
     * @return the rss
     */
    public boolean isRss() {
        return rss;
    }

    /**
     * @param rss the rss to set
     */
    public void setRss(boolean rss) {
        this.rss = rss;
    }

    /**
     * @return the diameterId
     */
    public long getDiameterId() {
        return diameterId;
    }

    /**
     * @param diameterId the diameterId to set
     */
    public void setDiameterId(long diameterId) {
        this.diameterId = diameterId;
    }
}

class ItemSegmento {
    private double tvdDesde;
    private double tvdHasta;
    private String tipo;
    private double sumatoriaDls;
    private double sumatoriaPorcentajeSliding;
    private double sumatoriaPromediosFlow;
    private double sumatoriaPromediosWob;
    private double sumatoriaPromediosDifferential;
    private double sumatoriaPromediosSrpm;
    private double sumatoriaPromediosTfAngle;
    private DescriptiveStatistics stats,stats2,stats3,stats4,stats5,stats6,stats7;
    private int n;  
    private String composicion;
    
    
    ItemSegmento(){
        stats=new DescriptiveStatistics(); //dls
        stats2=new DescriptiveStatistics(); //arena
        stats3=new DescriptiveStatistics(); //flow    
        stats4=new DescriptiveStatistics(); //Wob
        stats5=new DescriptiveStatistics(); //Differential
        stats6=new DescriptiveStatistics(); //Srpm
        stats7=new DescriptiveStatistics(); //TfAngle
        
        tvdDesde=0;
        tvdHasta=0;
        tipo="";
        sumatoriaDls=0;
        sumatoriaPorcentajeSliding=0;
        sumatoriaPromediosFlow=0;
        sumatoriaPromediosWob=0; 
        sumatoriaPromediosDifferential=0;
        sumatoriaPromediosSrpm=0;
        sumatoriaPromediosTfAngle=0;        
        n=0;
    }
    
    public String getComposicion() {
        return composicion;
    }    
    public void setComposicion(String v) {
        composicion=v;
    }    
    public void clearStats() {
        stats.clear();
    }
    public void addStatsValue(Double v) {
        stats.addValue(v);
    }
    public double statsStandardDeviation() {
        return stats.getStandardDeviation();
    }
    public long statsN(){
        return stats.getN();
    }
    public double statsMedia(){
        return stats.getMean();
    }
    public void clearStats2() {
        stats2.clear();
    }
    public void addStats2Value(Double v) {
        stats2.addValue(v);
    }
    public double stats2StandardDeviation() {
        return stats2.getStandardDeviation();
    }
    public long stats2N(){
        return stats2.getN();
    }
    public double stats2Media(){
        return stats2.getMean();
    }     
    public void clearStats3() {
        stats3.clear();
    }
    public void addStats3Value(Double v) {
        stats3.addValue(v);
    }
    public double stats3StandardDeviation() {
        return stats3.getStandardDeviation();
    }
    public long stats3N(){
        return stats3.getN();
    }
    public double stats3Media(){
        return stats3.getMean();
    }
    
    public void clearStats4() {
        stats4.clear();
    }
    public void addStats4Value(Double v) {
        stats4.addValue(v);
    }
    public double stats4StandardDeviation() {
        return stats4.getStandardDeviation();
    }
    public long stats4N(){
        return stats4.getN();
    }
    public double stats4Media(){
        return stats4.getMean();
    } 
    
    public void clearStats5() {
        stats5.clear();
    }
    public void addStats5Value(Double v) {
        stats5.addValue(v);
    }
    public double stats5StandardDeviation() {
        return stats5.getStandardDeviation();
    }
    public long stats5N(){
        return stats5.getN();
    }
    public double stats5Media(){
        return stats5.getMean();
    }
    
    public void clearStats6() {
        stats6.clear();
    }
    public void addStats6Value(Double v) {
        stats6.addValue(v);
    }
    public double stats6StandardDeviation() {
        return stats6.getStandardDeviation();
    }
    public long stats6N(){
        return stats6.getN();
    }
    public double stats6Media(){
        return stats6.getMean();
    }
    
    public void clearStats7() {
        stats7.clear();
    }
    public void addStats7Value(Double v) {
        stats7.addValue(v);
    }
    public double stats7StandardDeviation() {
        return stats7.getStandardDeviation();
    }
    public long stats7N(){
        return stats7.getN();
    }
    public double stats7Media(){
        return stats7.getMean();
    }
    
    public double getTvdDesde() {
        return tvdDesde;
    }
    public void setTvdDesde(double tvdDesde) {
        this.tvdDesde = tvdDesde;
    }
    public double getTvdHasta() {
        return tvdHasta;
    }
    public void setTvdHasta(double tvdHasta) {
        this.tvdHasta = tvdHasta;
    }
    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    public double getSumatoriaDls() {
        return sumatoriaDls;
    }
    public void setSumatoriaDls(double sumatoriaDls) {
        this.sumatoriaDls = sumatoriaDls;
    }
    public double getSumatoriaPorcentajeSliding() {
        return sumatoriaPorcentajeSliding;
    }
    public void setSumatoriaPorcentajeSliding(double sumatoriaPorcentajeSliding) {
        this.sumatoriaPorcentajeSliding = sumatoriaPorcentajeSliding;
    }
    
    public int getN() {
        return n;
    }
    public void setN(int n) {
        this.n = n;
    }

    public double getSumatoriaPromediosFlow() {
        return sumatoriaPromediosFlow;
    }

    public void setSumatoriaPromediosFlow(double sumatoriaPromediosFlow) {
        this.sumatoriaPromediosFlow = sumatoriaPromediosFlow;
    }

    public double getSumatoriaPromediosWob() {
        return sumatoriaPromediosWob;
    }

    public void setSumatoriaPromediosWob(double sumatoriaPromediosWob) {
        this.sumatoriaPromediosWob = sumatoriaPromediosWob;
    }

    public double getSumatoriaPromediosDifferential() {
        return sumatoriaPromediosDifferential;
    }

    public void setSumatoriaPromediosDifferential(double sumatoriaPromediosDifferential) {
        this.sumatoriaPromediosDifferential = sumatoriaPromediosDifferential;
    }

    public double getSumatoriaPromediosSrpm() {
        return sumatoriaPromediosSrpm;
    }

    public void setSumatoriaPromediosSrpm(double sumatoriaPromediosSrpm) {
        this.sumatoriaPromediosSrpm = sumatoriaPromediosSrpm;
    }

    public double getSumatoriaPromediosTfAngle() {
        return sumatoriaPromediosTfAngle;
    }

    public void setSumatoriaPromediosTfAngle(double sumatoriaPromediosTfAngle) {
        this.sumatoriaPromediosTfAngle = sumatoriaPromediosTfAngle;
    }
}

class MiRender extends DefaultTableCellRenderer
{
   public Component getTableCellRendererComponent(JTable table,
      Object value,
      boolean isSelected,
      boolean hasFocus,
      int row,
      int column)
   {
      super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
        if (value==null) return this;
        if (column==13) {
            try {
                if ((Double)value>=100.0){
                   this.setOpaque(true);
                   this.setBackground(Color.black);
                   this.setForeground(Color.WHITE);        
                } else {
                   if ((Double)value>=80.0){
                      this.setOpaque(true);
                      this.setBackground(Color.RED);
                      this.setForeground(Color.WHITE); 
                   } else {
                      if ((Double)value>=50.0){
                         this.setOpaque(true);
                         this.setBackground(Color.YELLOW);
                         this.setForeground(Color.black); 
                      } else {
                         if ((Double)value>=0.0){
                            this.setOpaque(true);
                            this.setBackground(Color.GREEN);
                            this.setForeground(Color.WHITE); 
                         } else {
                            this.setOpaque(false);
                            this.setBackground(Color.LIGHT_GRAY);
                            this.setForeground(Color.black);                             
                         }
                      } 
                   }
                }
            }catch (Exception ex){
               this.setOpaque(false);
               this.setBackground(Color.LIGHT_GRAY);
               this.setForeground(Color.black);
            }
        }else {                  
            if (column==17) {
                if ("Sand".equals(value.toString().trim())) {
                    this.setOpaque(true);
                    this.setBackground(Color.YELLOW);
                    this.setForeground(Color.BLACK);                  
                }
                if ("Shale".equals(value.toString().trim())) {
                    this.setOpaque(true);
                    this.setBackground(Color.DARK_GRAY);
                    this.setForeground(Color.WHITE);                 
                }
                if ("Shale-Sand intercalation".equals(value.toString().trim())) {
                    this.setOpaque(true);
                    this.setBackground(Color.ORANGE);
                    this.setForeground(Color.red);                 
                }            
            } else {
                this.setOpaque(false);
                this.setBackground(Color.LIGHT_GRAY);
                this.setForeground(Color.black);   
            }
        }

      return this;
   }
}