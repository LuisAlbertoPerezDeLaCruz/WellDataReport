/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import javax.swing.filechooser.FileNameExtensionFilter;
import miLibreria.Numeros;
import static miLibreria.GlobalConstants.valorNulo;
import static miLibreria.GlobalConstants.valorNuloMuestra;
import miLibreria.ManejoDeCombos;
import miLibreria.bd.*;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Luis
 */
public class Rpt003 extends javax.swing.JDialog {
    public ManejoBDI oBD;
    private ManejoDeCombos oManejoDeCombos; 
    private static DefaultListModel<String> modeloLista;   
    private String sDir;
    private long clienteId=0,campoId=0, macollaId=0;
    private Numeros oNumeros=new Numeros();
    private long wellId=-1;
    private String wellNombre="";
    private Object[][] aPozos = null;
    private Object[] ob_array = null;
    private JDialog Ventana;  
    private Calculos oCalc=null;
    
    public Rpt003(java.awt.Frame parent, boolean modal,ManejoBDI o ) {
        super(parent, modal);
        initComponents();
        oBD=o;
        oManejoDeCombos = new ManejoDeCombos();  
        modeloLista = new DefaultListModel<>();
        this.jListPozos.setModel(modeloLista);
        this.jRadioButtonDummy.setVisible(false);
        seteosIniciales();
    }
    
    public final void seteosIniciales() {
        int tiempoEnMilisegundos = 500;
        Timer timer = new Timer (tiempoEnMilisegundos, new ActionListener () { 
        public void actionPerformed(ActionEvent e) { 
            boolean ok=true;
            if (macollaId==0 && campoId==0 && clienteId==0) ok=false;
            jLabelExportarTVD.setEnabled(ok);
            jLabelExportarSummary.setEnabled(ok);
            if (wellId>0) {
                jLabelExportarTVD.setEnabled(true); 
                jLabelGraficar.setEnabled(true);
            }
            else {
                jLabelExportarTVD.setEnabled(false);
                jLabelGraficar.setEnabled(false);
            }
            pack();
        } 
        });
        timer.start();
    }
    
    private void buscarPozo() {
        String s;
        ResultSet rs;
        int i=0;
        int cantPozos=0;
        String prevPozo=null, currPozo=null;
        modeloLista.removeAllElements();
        limpiarTablaDatos();
        limpiarTablaSecciones();
        wellId=0;
        this.jLabelPozo.setText("Pozo: ");
        this.jTextPaneLeccionesAprendidas.setText("");
        
        try {
            this.jComboBoxClientes.setSelectedIndex(0);
            this.jComboBoxCampo.setSelectedIndex(0);
            this.jComboBoxMacolla.setSelectedIndex(0);
        } catch (IllegalArgumentException ex) {};

        this.jComboBoxClientes.setEnabled(false);
        this.jComboBoxCampo.setEnabled(false);
        this.jComboBoxMacolla.setEnabled(false);
        
        this.jRadioButtonDummy.setSelected(true);
        
        s="SELECT DISTINCT wellNombre, wellId ";
        if ("ManejoBDSqlServer".equals(oBD.getClass().getSimpleName())) {
           s+="FROM ConsultaCorridasPorCriteria1 WHERE CHARINDEX('"+this.jTextFieldBuscar.getText()+"',wellNombre)>0";
        }
        if ("ManejoBDAccess".equals(oBD.getClass().getSimpleName())) {
           s+="FROM ConsultaCorridasPorCriteria1 WHERE instr(wellNombre,'" + this.jTextFieldBuscar.getText()+"')>0";
        }
        if (campoId>0) {
            s+=" AND campoId="+campoId;    
        }
        if (macollaId>0) {
            s+=" AND macollaId="+macollaId;    
        } 
        s+=" ORDER BY wellNombre ASC;";
        
        rs=oBD.select(s);
        if (rs==null) return;
        
        try {            
            while (rs.next()) {                
                i++;
            }
            aPozos=new Object[i][2];
            i=0;
            
            rs.beforeFirst();
            while (rs.next()) {
                aPozos[cantPozos][0]=rs.getString("wellNombre");
                aPozos[cantPozos][1]=rs.getLong("wellId");
                modeloLista.addElement(aPozos[cantPozos][0].toString());
                cantPozos++;
                i++;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Rpt003.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 

    private void accionClickLista() {
        String s="";
        if (modeloLista.isEmpty()) return;
        try {
            s=modeloLista.getElementAt(this.jListPozos.getSelectedIndex());
            wellNombre=aPozos[this.jListPozos.getSelectedIndex()][0].toString();
            wellId=(Long) aPozos[this.jListPozos.getSelectedIndex()][1];
            ubicaMacollaCampoCliente(wellId);
            muestraInformacionPozo(wellId);
        }
        catch (ArrayIndexOutOfBoundsException ex){return;};
        this.jLabelPozo.setText("Pozo: "+s);
    }
    
    private void ubicaMacollaCampoCliente(long wellId) {
       long macollaId=0, campoClienteId, campoId=0, clienteId=0 ;
       String s;
       ResultSet rs;
       try {
           rs=oBD.select("SELECT macollaId FROM Well Where id="+wellId);
           rs.next();
           macollaId=rs.getLong("macollaId");
           rs=oBD.select("SELECT CampoClienteId FROM Macolla Where id="+macollaId);
           rs.next();
           campoClienteId=rs.getLong("campoClienteId");
           rs=oBD.select("SELECT clienteId, campoId FROM CampoCliente Where id="+campoClienteId);
           rs.next();
           clienteId=rs.getLong("clienteId");
           campoId=rs.getLong("campoId");
           
           oManejoDeCombos.llenaCombo(oBD,oManejoDeCombos.getModeloCombo(),Clientes.class,this.jComboBoxClientes,"Seleccione Cliente"); 
           oManejoDeCombos.setCombo(clienteId, this.jComboBoxClientes);
           
           s="SELECT campoId,campoNombre from ConsultaCampoCliente1 WHERE clienteId="+clienteId;
           oManejoDeCombos.llenaCombo(oBD,oManejoDeCombos.getModeloCombo(),s,this.jComboBoxCampo,"Seleccione Campo");          
           oManejoDeCombos.setCombo(campoId, this.jComboBoxCampo);
           
           s="SELECT macollaId,macollaNombre from ConsultaMacolla1 WHERE clienteId="+clienteId + " AND campoId="+campoId;
           oManejoDeCombos.llenaCombo(oBD,oManejoDeCombos.getModeloCombo(),s,this.jComboBoxMacolla,"Seleccione Macolla");
           oManejoDeCombos.setCombo(macollaId, this.jComboBoxMacolla);
           
           this.jComboBoxClientes.setEnabled(false);
           this.jComboBoxCampo.setEnabled(false);
           this.jComboBoxMacolla.setEnabled(false);
           
           pack();
       }
       catch (SQLException ex){};      
    }
    
    private void muestraInformacionPozo(long wellId) {
        String s;
        ResultSet rs;
        Well oWell=new Well();
        
        boolean firstTime=true;
        long prevSection=0, currSection=0;
        double md=0,md0=0, md1=0, acumBRT=0;
        long prevNumeroIdentificador=0,currNumeroIdentificador=0;
        long prevRun=0,currRun=0;
        int i=0,cantRuns=0;
        
        long prevDrillingSubSection=0,currDrillingSubSection=0;
        double prevMd=0,currMd=0, acumMd=0, acumHorizontal=0;
        
        Double[][] aMd=new Double[10][2];
        
        try {
            ob_array=oBD.select(Well.class, "id="+wellId);
            oWell=(Well) ob_array[0];
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Rpt003.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        this.jTextPaneLeccionesAprendidas.setText(oWell.getLeccionesAprendidas());
        

        try {           
            s="SELECT sectionsId,md,numeroIdentificador,runID,brt ";
            s+="FROM ConsultaSummary1 " ;
            s+="WHERE wellId="+wellId+" ";
            s+="ORDER BY sectionsId, md;";
            
            rs=oBD.select(s);
            while (rs.next()) {
                if (firstTime) {
                    prevSection=rs.getLong("sectionsId");
                    prevNumeroIdentificador=rs.getLong("numeroIdentificador");
                    prevRun=rs.getLong("runId");
                    currSection=prevSection;
                    currNumeroIdentificador=prevNumeroIdentificador;
                    currRun=prevRun;
                    md=rs.getDouble("md");
                    md0=md;
                    acumBRT=rs.getDouble("brt");
                    firstTime=false;
                } else {
                    prevSection=currSection;
                    prevNumeroIdentificador=currNumeroIdentificador;
                    prevRun=currRun;
                    currSection=rs.getLong("sectionsId");
                    currNumeroIdentificador=rs.getLong("numeroIdentificador");
                    currRun=rs.getLong("runId");
                    md1=rs.getDouble("md");
                    if (currRun!=prevRun) {
                        if (currSection == prevSection)
                            acumBRT+=rs.getDouble("brt");
                        cantRuns++;
                    }
                    if (currSection != prevSection) {
                        this.jTableSecciones.setValueAt(prevNumeroIdentificador, i, 0);
                        this.jTableSecciones.setValueAt(na(md1-md0), i, 1);
                        this.jTableSecciones.setValueAt(cantRuns, i, 2);
                        this.jTableSecciones.setValueAt(na(acumBRT), i, 3);
                        md0=md1;
                        i++;
                        cantRuns=1;
                        acumBRT=rs.getDouble("brt");
                    }
                }                
            }
            this.jTableSecciones.setValueAt(prevNumeroIdentificador, i, 0);
            this.jTableSecciones.setValueAt(na(md1-md0), i, 1);
            this.jTableSecciones.setValueAt(cantRuns, i, 2);
            this.jTableSecciones.setValueAt(na(acumBRT), i, 3);
            
            s="SELECT wellId, drillingSubSectionId, drillingSubSeccionDescription, md " ;
            s+="FROM ConsultaCorridasPorCriteria1 " ;
            s+="WHERE wellId="+wellId+" ";
            s+="ORDER by md ASC";
            
            this.jTableDatos.setValueAt("Long/Tanjente",0,0);
            this.jTableDatos.setValueAt("Long/Horizontal",1,0);
            this.jTableDatos.setValueAt("% arena seccion Hz",2,0);
            this.jTableDatos.setValueAt("Tipo de Pozo",3,0);
            this.jTableDatos.setValueAt("Azim antes Tang",4,0);
            this.jTableDatos.setValueAt("Azim despues Tang",5,0);
            this.jTableDatos.setValueAt("DDI ultima seccion",6,0);
            this.jTableDatos.setValueAt("ERD ultima seccion",7,0);
            this.jTableDatos.setValueAt("TORT ultima seccion",8,0);
            
            this.jTableDatos.setValueAt(0,0,1);
            this.jTableDatos.setValueAt(0,1,1);
            this.jTableDatos.setValueAt(0,2,1);
            this.jTableDatos.setValueAt("",3,1);
            this.jTableDatos.setValueAt(0,4,1);
            this.jTableDatos.setValueAt(0,5,1);
            this.jTableDatos.setValueAt(0,6,1);
            this.jTableDatos.setValueAt(0,7,1);
            this.jTableDatos.setValueAt(0,8,1);
            
            rs=oBD.select(s);
            firstTime=true;
            while (rs.next()) {
                if (firstTime) {
                    prevDrillingSubSection=rs.getLong("drillingSubSectionId");
                    currDrillingSubSection=prevDrillingSubSection;
                    prevMd=rs.getDouble("md");
                    currMd=prevMd;
                    acumMd+=0;
                    firstTime=false;
                } else {
                    prevDrillingSubSection=currDrillingSubSection;
                    currDrillingSubSection=rs.getLong("drillingSubSectionId");
                    prevMd=currMd;
                    currMd=rs.getDouble("md");

                    if (prevDrillingSubSection!=currDrillingSubSection){
                      if (prevDrillingSubSection==5 ) {
                         this.jTableDatos.setValueAt(na(acumMd),0,1);
                      }
                      if (prevDrillingSubSection==9 || prevDrillingSubSection==10 || prevDrillingSubSection==11 ) {
                          acumHorizontal+=acumMd;
                      }
                      acumMd=0;  
                    }
                    acumMd+=(currMd-prevMd);                    
                }                    
            }
            if (prevDrillingSubSection==5 ) {
               this.jTableDatos.setValueAt(na(acumMd),0,1);
            }
            if (prevDrillingSubSection==9 || prevDrillingSubSection==10 || prevDrillingSubSection==11 ) {
                acumHorizontal+=acumMd;
            } 
            this.jTableDatos.setValueAt(na(acumHorizontal),1,1);
            
            s= "SELECT wellId, drillingSubSectionId, Max(md) AS [max] ";
            s+="FROM ConsultaCorridasPorCriteria1 ";
            s+="GROUP BY wellId, drillingSubSectionId ";
            s+="HAVING wellId=" + wellId + " ";
            s+="ORDER BY Max(ConsultaCorridasPorCriteria1.md);";
            
            rs=oBD.select(s);
            int p=0;
            while (rs.next()) {
                aMd[p][0]=new Double(rs.getLong("drillingSubSectionId"));
                aMd[p][1]=new Double(rs.getDouble("max"));
                p++;
            }
            double mdFrom=0,mdTo=0,deltaMd=0,acumDeltaMd=0, porcentajeHz=0;
            long cantReg=0;
            
            for (int x=0;x<=p-1;x++) {
                if (aMd[x][0]>=9 && aMd[x][0]<=11) {
                    
                    mdFrom=(x==0)?0:aMd[x-1][1];
                    mdTo=aMd[x][1];
                    
                    deltaMd=mdTo-mdFrom;
                    acumDeltaMd+=deltaMd;
                    
                    s= "SELECT Sections.wellId, LASPerMD.dept, LASPerMD.gr ";
                    s+="FROM LASPerMD INNER JOIN (LAS INNER JOIN (Run INNER JOIN Sections ON Run.sectionId = Sections.id) ON LAS.runId = Run.Id) ON LASPerMD.lasId = LAS.id ";
                    s+="WHERE (((Sections.wellId)="+ wellId +") AND ((LASPerMD.dept)>="+mdFrom+" And (LASPerMD.dept)<="+mdTo+") AND ((LASPerMD.gr)>0) AND ((LASPerMD.gr)<=45)) ";
                    s+="ORDER BY LASPerMD.dept;";
                    
                    rs=oBD.select(s);
                    
                    while (rs.next()) {
                        cantReg++;
                    }
                    
                }
            }
            porcentajeHz=((cantReg/2)/(acumDeltaMd))*100;                    
            this.jTableDatos.setValueAt(na(porcentajeHz),2,1);
            
            s="SELECT Well.id, Well.nombre, Well.wellTypeId, WellType.nombre ";
            s+="FROM WellType INNER JOIN Well ON WellType.Id = Well.wellTypeId ";
            s+="WHERE Well.id="+wellId;
            
            rs=oBD.select(s);

            if (rs.next()) {
               this.jTableDatos.setValueAt(rs.getString("WellType.nombre"),3,1);
            }
            
            int nro=0;
            String s1=null;
            for (int x=0;x<=this.jTableSecciones.getRowCount()-1;x++) {
                s1=this.jTableSecciones.getValueAt(x, 0).toString();
                if (s1=="") break;
                nro=new Integer(s1);
                s="SELECT wellId, sectionsId, sectionsNumeroIdentificador, Min(dls) AS [Min], Max(dls) AS [Max] ";
                s+="FROM ConsultaCorridasPorCriteria1 ";
                s+="GROUP BY wellId, sectionsId, sectionsNumeroIdentificador ";
                s+="HAVING wellId="+wellId+" AND sectionsNumeroIdentificador="+nro+";";  
                rs=oBD.select(s);

                if (rs.next()) {
                   this.jTableSecciones.setValueAt(na(rs.getDouble("Max")),x,4);
                   this.jTableSecciones.setValueAt(na(rs.getDouble("Min")),x,5);
                }
                
            }
            
            s="SELECT wellId, drillingSubSectionId, drillingSubSeccionDescription, Avg(azim) AS PromedioDeazim " ;
            s+="FROM ConsultaCorridasPorCriteria1 " ;
            s+="GROUP BY wellId, drillingSubSectionId, drillingSubSeccionDescription " ;
            s+="HAVING wellId="+wellId+" ORDER BY drillingSubSectionId ASC;";
            
            rs=oBD.select(s);
            double prevAzim,currAzim;
            firstTime=true;
            Double[][] azim=new Double[10][2];
            int x=0,x1=0;
            while (rs.next()) {
                azim[x][0]=new Double(rs.getLong("drillingSubSectionId"));
                azim[x][1]=rs.getDouble("PromedioDeazim");
                if(azim[x][0]==5) x1=x;
                x++;
            }
            try {
                if (x1>0) {
                    this.jTableDatos.setValueAt(na(azim[x1-1][1]),4,1);
                    this.jTableDatos.setValueAt(na(azim[x1+1][1]),5,1);
                }
            } catch (NullPointerException e) {}
            
            s="SELECT ConsultaCorridasPorPozo.Well.Id, ConsultaCorridasPorPozo.Sections.Id, ConsultaCorridasPorPozo.numeroIdentificador, ConsultaCorridasPorPozo.Run.Id, Survey.ddi, Survey.erd, Survey.tort " ;
            s+="FROM Survey INNER JOIN ConsultaCorridasPorPozo ON Survey.runId = ConsultaCorridasPorPozo.Run.Id " ;
            s+="WHERE ConsultaCorridasPorPozo.Well.Id="+wellId+" " ;
            s+="ORDER BY ConsultaCorridasPorPozo.Sections.Id DESC, ConsultaCorridasPorPozo.Run.Id DESC;";
            
            s="SELECT Well.id, Sections.id, Sections.numeroIdentificador, Run.Id, Run.numero, Survey.tort, Survey.ddi, Survey.erd " ;
            s+="FROM Survey INNER JOIN (Run INNER JOIN (Sections INNER JOIN Well ON Sections.wellId = Well.id) ON Run.sectionId = Sections.id) ON Survey.runId = Run.Id " ;
            s+="WHERE Well.id="+wellId + " ";
            s+="ORDER BY Well.id, Sections.id DESC , Run.Id DESC;";
            
            rs=oBD.select(s);
            
            if (rs.next()) {
                this.jTableDatos.setValueAt(na(rs.getDouble("ddi")),6,1);
                this.jTableDatos.setValueAt(na(rs.getDouble("erd")),7,1);
                this.jTableDatos.setValueAt(na(rs.getDouble("tort")),8,1);
            }
            
        } catch (SQLException ex) {
            return;
        }
    }
    
    private void prepararPloteosyGraficar() {
        double [][] ploteo1=null,ploteo2=null,ploteo3=null,ploteo4=null;
        cursorEspera();
        this.jLabelGraficar.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        ploteo1=graficarTvdVsSlidingSteering();        
        ploteo2=graficarTvdVsDlsOverDeltaMDSlidingSteering();
        ploteo3=graficarTvdVsDls();
        ploteo4=graficarTvdVsGr();
        this.jLabelGraficar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cursorNormal();
        graficar(ploteo1.length,ploteo1,ploteo2.length,ploteo2,ploteo3.length,ploteo3,ploteo4.length,ploteo4);
    }
        
    private double [][] graficarTvdVsSlidingSteering() {
        double [][] ploteo=null;
        TvdDetail oTD=null;
        String s;
        Calculos oCalc=new Calculos(oBD);
        oCalc.cargarPozo(wellId);
        SurveyPerMD oSurveyPerMd=new SurveyPerMD();
        ploteo=new double[oCalc.getSurveyPerMd().length][3];
        for (int i=0;i<=oCalc.getSurveyPerMd().length-1;i++) {
            oSurveyPerMd=oCalc.getSurveyPerMd()[i];
            ploteo[i][0]=oCalc.getTVD(oSurveyPerMd.getMd());
            ploteo[i][1]=oCalc.getPorcentajeSliding(oSurveyPerMd.getMd());
            ploteo[i][2]=oCalc.getPorcentajeSteering(oSurveyPerMd.getMd());
        }        
        return ploteo;                
    }
    
    private double [][] graficarTvdVsDlsOverDeltaMDSlidingSteering() {
        double [][] ploteo=null;
        TvdDetail oTD=null;
        String s;
        Calculos oCalc=new Calculos(oBD);
        oCalc.cargarPozo(wellId);
        SurveyPerMD oSurveyPerMd=new SurveyPerMD();
        ploteo=new double[oCalc.getSurveyPerMd().length][3];
        for (int i=0;i<=oCalc.getSurveyPerMd().length-1;i++) {
            oSurveyPerMd=oCalc.getSurveyPerMd()[i];
            ploteo[i][0]=oCalc.getTVD(oSurveyPerMd.getMd());
            ploteo[i][1]=oCalc.getDlsOverDeltaMdSliding(oSurveyPerMd.getMd());
            ploteo[i][2]=oCalc.getDlsOverDeltaMdSteering(oSurveyPerMd.getMd());
        }        
        return ploteo;        
    }
    
    private double [][] graficarTvdVsDls() {
        double [][] ploteo=null;
        TvdDetail oTD=null;
        String s;
        Calculos oCalc=new Calculos(oBD);
        oCalc.cargarPozo(wellId);
        SurveyPerMD oSurveyPerMd=new SurveyPerMD();
        ploteo=new double[oCalc.getSurveyPerMd().length][2];
        for (int i=0;i<=oCalc.getSurveyPerMd().length-1;i++) {
            oSurveyPerMd=oCalc.getSurveyPerMd()[i];
            ploteo[i][0]=oCalc.getTVD(oSurveyPerMd.getMd());
            ploteo[i][1]=oCalc.getDls(oSurveyPerMd.getMd());
         }        
        return ploteo;                
    }
    
    private double [][] graficarTvdVsGr() {
        double [][] ploteo=null;
        TvdDetail oTD=null;
        String s;
        Calculos oCalc=new Calculos(oBD);
        oCalc.cargarPozo(wellId);
        SurveyPerMD oSurveyPerMd=new SurveyPerMD();
        ploteo=new double[oCalc.getSurveyPerMd().length][2];
        for (int i=0;i<=oCalc.getSurveyPerMd().length-1;i++) {
            oSurveyPerMd=oCalc.getSurveyPerMd()[i];
            ploteo[i][0]=oCalc.getTVD(oSurveyPerMd.getMd());
            ploteo[i][1]=oCalc.getGR(oSurveyPerMd.getMd());
         }        
        return ploteo;                
    }
       
    private int comoPertenece(double[] par1, double[] par2) {
        int result=-1;
        final int desde=0, hasta=1;
        final int quedaParcialPorAbajo=0,quedaParcialPorArriba=1,quedaTotal=2,quedaPorDebajo=3,quedaPorArriba=4;
        
        //Devuelve 0 si el par1 se encuentra parcialmente en el par 2 por debajo
        //Devuelve 1 si el par1 se encuentra parcialmente en el par 2 por arriba
        //Devuelve 2 si el par1 se encuentra totalmente en el par2
        //Devuelve 3 si queda por debajo
        //Devuelve 4 si queda por arriba

        if (par1[desde]>=par2[desde] && par1[hasta]>par2[hasta]) result=quedaParcialPorArriba;
        if (par1[desde]<par2[desde] && par1[hasta]>par2[desde]) result=quedaParcialPorAbajo;
        
        if (par1[desde]>=par2[desde] && par1[hasta]<=par2[hasta]) result=quedaTotal;
        if (par1[hasta]<par2[desde]) result=quedaPorDebajo;
        if (par1[desde]>par2[hasta]) result=quedaPorArriba;
        
        return result;
    }    
    
    private void graficar(int rowsIn1,double[][] ploteo1,int rowsIn2,double[][] ploteo2,int rowsIn3,double[][] ploteo3,int rowsIn4,double[][] ploteo4) {
        JFreeChart oJFreeChart1 = ChartFactory.createXYLineChart(
            "TVD vs %Steering" ,
            "TVD (feet)" ,
            "%Steering" ,
            createDataset(rowsIn1, ploteo1) ,
            PlotOrientation.HORIZONTAL ,
            true , true , true);
        
        JFreeChart oJFreeChart2 = ChartFactory.createXYLineChart(
            "TVD vs dls/delta md Steering" ,
            "TVD (feet)" ,
            "dls/delta md Steering" ,
            createDataset(rowsIn2, ploteo2) ,
            PlotOrientation.HORIZONTAL ,
            true , true , true); 
        
        JFreeChart oJFreeChart3 = ChartFactory.createXYLineChart(
            "TVD vs dls" ,
            "TVD (feet)" ,
            "dls" ,
            createDataset(rowsIn3, ploteo3) ,
            PlotOrientation.HORIZONTAL ,
            false , true , false); 
        
        JFreeChart oJFreeChart4 = ChartFactory.createXYLineChart(
            "TVD vs gr" ,
            "TVD (feet)" ,
            "gr" ,
            createDataset(rowsIn4, ploteo4) ,
            PlotOrientation.HORIZONTAL ,
            false , true , false); 
        
        XYPlot plot1 = (XYPlot) oJFreeChart1.getPlot();        
        plot1.getRenderer().setSeriesPaint(0, Color.green.darker());        
        oJFreeChart1.getXYPlot().getDomainAxis().setInverted(true);        
        ChartPanel Panel1 = new ChartPanel(oJFreeChart1);
        
        XYPlot plot2 = (XYPlot) oJFreeChart2.getPlot();        
        plot2.getRenderer().setSeriesPaint(0, Color.blue.darker());        
        oJFreeChart2.getXYPlot().getDomainAxis().setInverted(true);        
        ChartPanel Panel2 = new ChartPanel(oJFreeChart2);
        
        XYPlot plot3 = (XYPlot) oJFreeChart3.getPlot();        
        plot3.getRenderer().setSeriesPaint(0, Color.red.darker());        
        oJFreeChart3.getXYPlot().getDomainAxis().setInverted(true);        
        ChartPanel Panel3 = new ChartPanel(oJFreeChart3);
        
        XYPlot plot4 = (XYPlot) oJFreeChart4.getPlot();        
        plot4.getRenderer().setSeriesPaint(0, Color.black.darker());        
        oJFreeChart4.getXYPlot().getDomainAxis().setInverted(true);        
        ChartPanel Panel4 = new ChartPanel(oJFreeChart4);
        
        Ventana = new JDialog(this,false);
        Ventana.setPreferredSize(new Dimension(900,680));
        Ventana.setMinimumSize(new Dimension(900,680));
        Ventana.setTitle("Graficas de TVD");
 
        JPanel jp=new JPanel();
        jp.setLayout(new GridLayout());
        jp.add(Panel1);
        jp.add(Panel2);
        jp.add(Panel3);
        jp.add(Panel4);

        Ventana.add(jp);

        Ventana.pack();
        Ventana.setLocationRelativeTo(this);
        Ventana.setModal(true);
        Ventana.setVisible(true);
        Ventana.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
        
    private XYDataset createDataset(int rows, double[][] ploteo ) {
       final XYSeries serie1 = new XYSeries( "Sliding" );
       final XYSeries serie2 = new XYSeries( "Steering" );
       for (int i=0;i<=rows-1;i++) {
            if (ploteo[0].length==2){
                try {
                    serie1.add( ploteo[i][0] , ploteo[i][1] );
                }catch (NumberFormatException ex) {}
            }
            if (ploteo[0].length==3) {
                  try {
                      serie1.add( ploteo[i][0] , ploteo[i][1] );
                      serie2.add( ploteo[i][0] , ploteo[i][2] );
                  }catch (NumberFormatException ex) {}
            } 
       }
       
       final XYSeriesCollection dataset = new XYSeriesCollection( );  
       dataset.addSeries( serie1 ); 
       if (ploteo[0].length==3)
            dataset.addSeries( serie2 );
       return dataset;
    }

    private void prepararControlesDeCriteria() {
        borrarInfoPozo();
        if (this.jRadioButtonCliente.isSelected()) {
            this.jComboBoxClientes.setEnabled(true);
            this.jComboBoxCampo.setEnabled(false);
            this.jComboBoxMacolla.setEnabled(false);
        }
        if (this.jRadioButtonCampo.isSelected()) {
            this.jComboBoxClientes.setEnabled(true);
            this.jComboBoxCampo.setEnabled(true);
            this.jComboBoxMacolla.setEnabled(false);            
        }
        if (this.jRadioButtonMacolla.isSelected()) {
            this.jComboBoxClientes.setEnabled(true);
            this.jComboBoxCampo.setEnabled(true);
            this.jComboBoxMacolla.setEnabled(true);            
        }
        oManejoDeCombos.llenaCombo(oBD,oManejoDeCombos.getModeloCombo(),Clientes.class,this.jComboBoxClientes,"Seleccione Cliente"); 
    }
    
    private void borrarInfoPozo() {
        for (int i=0;i<=this.jTableDatos.getRowCount()-1;i++) {
            for (int j=0;j<=this.jTableDatos.getColumnCount()-1;j++) {
                this.jTableDatos.setValueAt("", i, j);
            }            
        }
        for (int i=0;i<=this.jTableSecciones.getRowCount()-1;i++) {
            for (int j=0;j<=this.jTableSecciones.getColumnCount()-1;j++) {
                this.jTableSecciones.setValueAt("", i, j);
            }            
        }
        this.jTextPaneLeccionesAprendidas.setText("");
        this.jLabelPozo.setText("Pozo:");
        modeloLista.removeAllElements();
        this.jTextFieldBuscar.setText("");
        wellId=0;
    }
    
    public Object na(Object o) {
        DecimalFormat df = new DecimalFormat("##########0.000");
        if (o.toString().contains(""+valorNulo)) {
            return valorNuloMuestra;
        } else {
            try {
            return df.format(o).replace(',', '.');
            } catch (IllegalArgumentException ex) {
                return o;
            }
        }
    }
    
    public void msgbox(String s, String t){
        JOptionPane.showMessageDialog(null, s,t,TrayIcon.MessageType.WARNING.ordinal());        
    }
    
    public void msgbox(String s, String t, boolean ErrorGrave){
        if (ErrorGrave)
            JOptionPane.showMessageDialog(null, s,t,TrayIcon.MessageType.ERROR.ordinal());
        else
            JOptionPane.showMessageDialog(null, s,t,TrayIcon.MessageType.WARNING.ordinal());
        
    }

    private void limpiarTablaSecciones() {
        for (int i=0;i<=this.jTableSecciones.getRowCount()-1;i++) {
            for (int j=0;j<=this.jTableSecciones.getColumnCount()-1;j++) {
                this.jTableSecciones.setValueAt("", i, j);
            }
        }
    }
    
    private void limpiarTablaDatos() {
        for (int i=0;i<=this.jTableDatos.getRowCount()-1;i++) {
            for (int j=0;j<=this.jTableDatos.getColumnCount()-1;j++) {
                this.jTableDatos.setValueAt("", i, j);
            }
        }
    }
    
    public boolean crearArchivo_TvdDetail(TvdDetail[][] aTD, String archivo) {
        boolean ok=true;
        HSSFSheet sheet=null;
        HSSFRow[] rowhead=null;
        
        String filename = archivo ; 
        HSSFWorkbook workbook = new HSSFWorkbook(); 
        
        TvdDetail oTD=new TvdDetail();
        
        int row=1;
        
        for (int i=0;i<=aTD.length-1;i++) {
            if (aTD[i][0] != null) {
                sheet = workbook.createSheet("Seccion "+i);
                rowhead=new HSSFRow[aTD[0].length] ;
                for (int r=0;r<=rowhead.length-1;r++) {
                    rowhead[r] = sheet.createRow((short) r); 
                } 
                row=1;
               
                rowhead[row].createCell(1).setCellValue("well");
                rowhead[row].createCell(2).setCellValue("TVD");
                rowhead[row].createCell(3).setCellValue("MD");
                rowhead[row].createCell(4).setCellValue("GR");
                rowhead[row].createCell(5).setCellValue("dls");
                
                rowhead[row].createCell(6).setCellValue("% Sliding");
                rowhead[row].createCell(7).setCellValue("dls/deltaMdSliding");

                rowhead[row].createCell(8).setCellValue("% Steering");
                rowhead[row].createCell(9).setCellValue("dls/deltaMdSteering");                    
             
                rowhead[row].createCell(10).setCellValue("% arena");
                rowhead[row].createCell(11).setCellValue("clasificacion");
                
                rowhead[row].createCell(12).setCellValue("Tipo Sub-Seccion");

                for  (int j=0;j<=aTD[0].length-1;j++) {                
                    if (aTD[i][j] != null) {
                        oTD=aTD[i][j];
                        row++;
                        rowhead[row].createCell(1).setCellValue(oTD.wellNombre);
                        rowhead[row].createCell(2).setCellValue(oTD.tvd);
                        rowhead[row].createCell(3).setCellValue(oTD.md);
                        rowhead[row].createCell(4).setCellValue(oTD.gr);
                        rowhead[row].createCell(5).setCellValue(oTD.dls);
                    
                        rowhead[row].createCell(6).setCellValue(oTD.porcentajeSliding);
                        rowhead[row].createCell(7).setCellValue(oTD.dlsPorDeltaMdSliding);

                        rowhead[row].createCell(8).setCellValue(oTD.porcentajeSteering);
                        rowhead[row].createCell(9).setCellValue(oTD.dlsPorDeltaMdSteering);                            

                        rowhead[row].createCell(10).setCellValue(oTD.porcentajeArena);
                        rowhead[row].createCell(11).setCellValue(oTD.clasificacion);
                        
                        rowhead[row].createCell(12).setCellValue(oTD.drillingSubSectionType);
                    } else break; 
                }
            }
        }
        try {
            FileOutputStream fileOut;            
            fileOut = new FileOutputStream(filename);
            workbook.write(fileOut);
            fileOut.close();
        } catch (Exception ex) {
                msgbox("Ocurrio un error, posiblemente el archivo de destino se encuentra abierto","Error",true);
                ok=false;
        }
        return ok;
    }
       
    public boolean crearArchivo_Summary(WellSummary[] aWS, String archivo) {
        boolean ok=true;
        HSSFSheet sheet=null;
        HSSFRow[] rowhead=new HSSFRow[30] ;
        WellSummary o=new WellSummary();
        
        String filename = archivo ; 
        HSSFWorkbook workbook = new HSSFWorkbook(); 
        
        for (int x=0;x<=aWS.length-1;x++) {
            o=aWS[x];  
            sheet = workbook.createSheet(o.wellNombre);  
        
            try {

                for (int i=0;i<=rowhead.length-1;i++) {
                    rowhead[i] = sheet.createRow((short) i); 
                }

                rowhead[1].createCell(1).setCellValue("Division: ");
                rowhead[1].createCell(2).setCellValue(o.divisionNombre);

                rowhead[2].createCell(1).setCellValue("Cliente: ");
                rowhead[2].createCell(2).setCellValue(o.clienteNombre);

                rowhead[3].createCell(1).setCellValue("Campo: ");
                rowhead[3].createCell(2).setCellValue(o.campoNombre);

                rowhead[4].createCell(1).setCellValue("Macolla: ");
                rowhead[4].createCell(2).setCellValue(o.macollaNombre);

                rowhead[5].createCell(1).setCellValue("Pozo: ");
                rowhead[5].createCell(2).setCellValue(o.wellNombre);

                rowhead[1].createCell(6).setCellValue("Lecciones Aprendidas: "+o.leccionesAprendidas);

                sheet.addMergedRegion(new CellRangeAddress(1,5,6,12));

                CellStyle style;

                CellUtil.setAlignment(CellUtil.getCell(rowhead[1], 6), workbook, CellStyle.ALIGN_JUSTIFY);

                int offset=8;

                for (int i=1;i<=o.datos.length-1;i++) {
                    try {
                        if (i==0||i==4) {
                            rowhead[offset+i].createCell(1).setCellValue(o.datos[i][0].toString()+":");
                            rowhead[offset+i].createCell(3).setCellValue(o.datos[i][1].toString());
                        }else {
                            rowhead[offset+i].createCell(1).setCellValue(o.datos[i][0].toString()+":");
                            rowhead[offset+i].createCell(3).setCellValue(new Double(o.datos[i][1].toString()));                        
                        }
                    } catch (NullPointerException | ClassCastException ex) {System.out.print(ex);}
                }

                offset=9;
                for (int i=0;i<=o.infoSecciones.length-1;i++) {
                    try {
                        if (i==0) {
                            for (int j=0;j<=o.infoSecciones[0].length-1;j++) {
                                rowhead[offset+i].createCell(j+6).setCellValue(o.infoSecciones[i][j].toString());
                            }
                        } else {
                            for (int j=0;j<=o.infoSecciones[0].length-1;j++) {
                                rowhead[offset+i].createCell(j+6).setCellValue(new Double(o.infoSecciones[i][j].toString()));
                            }                     
                        }                        
                    }catch (NullPointerException | ClassCastException ex) {System.out.print(ex);}        
                }

            } catch ( Exception ex ) {
                System.out.println(ex);
                ok=false;
            }
        
        }
               
        try {
            FileOutputStream fileOut;            
            fileOut = new FileOutputStream(filename);
            workbook.write(fileOut);
            fileOut.close();
        } catch (Exception ex) {
                msgbox("Ocurrio un error, posiblemente el archivo de destino se encuentra abierto","Error",true);
                ok=false;
        }
        
        
        return ok;
    } 
    
    private TvdDetail[][] armarTvdDetail(long wellId) {
        TvdDetail oTD;
        TvdDetail[][] aTD=new TvdDetail[10][1000] ;
        Sections oSection=null;
        SurveyPerMD oSurveyPerMd=null;
        String s;
        Calculos oCalc=new Calculos(oBD);
        Calculos oCalcSection=new Calculos(oBD);
        oCalc.cargarSecciones(wellId);
        int row=0;
        for (int i=0;i<=oCalc.getSections().length-1;i++) {
            oSection=oCalc.getSections()[i];
            oCalcSection.cargarPozo(wellId, oSection.getId());
            row=0;
            for (int j=0; j<=oCalcSection.getSurveyPerMd().length-1; j++) {
                oSurveyPerMd=oCalcSection.getSurveyPerMd()[j];
                oTD=new TvdDetail();
                oTD.seccionId=oSection.getId();
                oTD.wellId=wellId;
                oTD.wellNombre=oCalcSection.getWellNombre();
                oTD.md=oSurveyPerMd.getMd();
                oTD.gr=oCalcSection.getGR(oTD.md);
                oTD.tvd=oCalcSection.getTVD(oTD.md);
                oTD.dls=oCalcSection.getDls(oTD.md);
                oTD.seccionNumeroIdentificador=oSection.getNumeroIdentificador();
                oTD.porcentajeSliding=oCalcSection.getPorcentajeSliding(oTD.md);
                oTD.porcentajeSteering=oCalcSection.getPorcentajeSteering(oTD.md);
                oTD.porcentajeArena=oCalcSection.getPorcentajeArena(oTD.md);
                oTD.clasificacion=oCalcSection.getClasificacion(oTD.md);
                oTD.dlsPorDeltaMdSliding=oCalcSection.getDlsOverDeltaMdSliding(oTD.md);
                oTD.dlsPorDeltaMdSteering=oCalcSection.getDlsOverDeltaMdSteering(oTD.md);
                oTD.drillingSubSectionType=oCalc.getDrillingSubSectionType(oTD.md);
                aTD[(int)oTD.seccionNumeroIdentificador][row]=oTD;
                row++;                
            }
        }
        return aTD;
    }
        
    private boolean esRS(long runId) {
        boolean es=false;
        BHA oBHA=new BHA();
        try {
            oBHA=(BHA) oBD.select(BHA.class,"runID="+runId)[0];
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Rpt001.class.getName()).log(Level.SEVERE, null, ex);
        }
        if ("RSS".equals(oBHA.getTipoDT())) es=true;
        return es;
    }
    
    private double dameElDeptParaBuscarElGr(double md) {
        double gr=0.0;
        long iPart;
        double fPart;
        iPart = (long) md;
        fPart = md - iPart;
        if (fPart>0.5) {
            iPart++;
            gr=iPart;
        } else {
            if (fPart>=0.25 && fPart<=0.5) {
                gr=iPart+0.5;
            } else gr=iPart;
        }
        return gr;
    }
  
    private WellSummary armarInfoPozo(long wellId) {
        WellSummary oWS=new WellSummary();
        oWS.wellId=wellId;
        String s;
        ResultSet rs;
        Well oWell=new Well();
        
        boolean firstTime=true;
        long prevSection=0, currSection=0;
        double md=0,md0=0, md1=0, acumBRT=0;
        long prevNumeroIdentificador=0,currNumeroIdentificador=0;
        long prevRun=0,currRun=0;
        int i=0,cantRuns=0;
        
        long prevDrillingSubSection=0,currDrillingSubSection=0;
        double prevMd=0,currMd=0, acumMd=0, acumHorizontal=0;
        
        Double[][] aMd=new Double[10][2];
        
        try {
            ob_array=oBD.select(Well.class, "id="+wellId);
            oWell=(Well) ob_array[0];
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Rpt003.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        oWS.leccionesAprendidas=oWell.getLeccionesAprendidas();

        try {
            
            s="SELECT Well.id, Well.nombre as WellNombre, Macolla.nombre as MacollaNombre, Clientes.nombre as ClienteNombre, Campo.nombre as CampoNombre, Division.Nombre as DivisionNombre " ;
            s+="FROM Division INNER JOIN (Campo INNER JOIN (Clientes INNER JOIN (CampoCliente INNER JOIN (Macolla INNER JOIN Well ON Macolla.id = Well.macollaId) ON CampoCliente.id = Macolla.campoClienteId) ON Clientes.id = CampoCliente.clienteId) ON Campo.id = CampoCliente.campoId) ON Division.Id = Clientes.divisionId " ;
            s+="WHERE Well.id="+wellId+";";

            rs=oBD.select(s);

            if (rs.next()) {
               oWS.wellNombre=rs.getString("WellNombre");
               oWS.macollaNombre=rs.getString("MacollaNombre");
               oWS.divisionNombre=rs.getString("DivisionNombre");
               oWS.clienteNombre=rs.getString("ClienteNombre");
               oWS.campoNombre=rs.getString("CampoNombre");
            }            
            
            s="SELECT sectionsId,md,numeroIdentificador,runID,brt ";
            s+="FROM ConsultaSummary1 " ;
            s+="WHERE wellId="+wellId+" ";
            s+="ORDER BY sectionsId, md;";
            
            oWS.infoSecciones[0][0]="Seccion #";
            oWS.infoSecciones[0][1]="long/md";
            oWS.infoSecciones[0][2]="corridas";
            oWS.infoSecciones[0][3]="brt";
            oWS.infoSecciones[0][4]="dls Max";
            oWS.infoSecciones[0][5]="dls Min";
            
            rs=oBD.select(s);
            while (rs.next()) {
                if (firstTime) {
                    prevSection=rs.getLong("sectionsId");
                    prevNumeroIdentificador=rs.getLong("numeroIdentificador");
                    prevRun=rs.getLong("runId");
                    currSection=prevSection;
                    currNumeroIdentificador=prevNumeroIdentificador;
                    currRun=prevRun;
                    md=rs.getDouble("md");
                    md0=md;
                    acumBRT=rs.getDouble("brt");
                    firstTime=false;
                } else {
                    prevSection=currSection;
                    prevNumeroIdentificador=currNumeroIdentificador;
                    prevRun=currRun;
                    currSection=rs.getLong("sectionsId");
                    currNumeroIdentificador=rs.getLong("numeroIdentificador");
                    currRun=rs.getLong("runId");
                    md1=rs.getDouble("md");
                    if (currRun!=prevRun) {
                        if (currSection == prevSection)
                            acumBRT+=rs.getDouble("brt");
                        cantRuns++;
                    }
                    if (currSection != prevSection) {                        
                        oWS.infoSecciones[i+1][0]=prevNumeroIdentificador;
                        oWS.infoSecciones[i+1][1]=na(md1-md0);
                        oWS.infoSecciones[i+1][2]=cantRuns;
                        oWS.infoSecciones[i+1][3]=na(acumBRT);
                        
                        md0=md1;
                        i++;
                        cantRuns=1;
                        acumBRT=rs.getDouble("brt");
                    }
                }                
            }
            
            oWS.infoSecciones[i+1][0]=prevNumeroIdentificador;
            oWS.infoSecciones[i+1][1]=na(md1-md0);
            oWS.infoSecciones[i+1][2]=cantRuns;
            oWS.infoSecciones[i+1][3]=na(acumBRT);
            
            s="SELECT wellId, drillingSubSectionId, drillingSubSeccionDescription, md " ;
            s+="FROM ConsultaCorridasPorCriteria1 " ;
            s+="WHERE wellId="+wellId+" ";
            s+="ORDER by md ASC";
            
            oWS.datos[0][0]="descripcion";
            oWS.datos[0][1]="valor";
            oWS.datos[1][0]="Long/Tanjente";
            oWS.datos[2][0]="Long/Horizontal";
            oWS.datos[3][0]="% arena seccion Hz";
            oWS.datos[4][0]="Tipo de Pozo";
            oWS.datos[5][0]="Azim antes Tang";
            oWS.datos[6][0]="Azim despues Tang";
            oWS.datos[7][0]="DDI ultima seccion";
            oWS.datos[8][0]="ERD ultima seccion";
            oWS.datos[9][0]="TORT ultima seccion";
            
            oWS.datos[1][1]=0;
            oWS.datos[2][1]=0;
            oWS.datos[3][1]=0;
            oWS.datos[4][1]="";
            oWS.datos[5][1]=0;
            oWS.datos[6][1]=0;
            oWS.datos[7][1]=0;
            oWS.datos[8][1]=0;
            oWS.datos[9][1]=0;
            
            rs=oBD.select(s);
            firstTime=true;
            while (rs.next()) {
                if (firstTime) {
                    prevDrillingSubSection=rs.getLong("drillingSubSectionId");
                    currDrillingSubSection=prevDrillingSubSection;
                    prevMd=rs.getDouble("md");
                    currMd=prevMd;
                    acumMd+=0;
                    firstTime=false;
                } else {
                    prevDrillingSubSection=currDrillingSubSection;
                    currDrillingSubSection=rs.getLong("drillingSubSectionId");
                    prevMd=currMd;
                    currMd=rs.getDouble("md");

                    if (prevDrillingSubSection!=currDrillingSubSection){
                      if (prevDrillingSubSection==5 ) {
                         oWS.datos[1][1]=na(acumMd);
                      }
                      if (prevDrillingSubSection==9 || prevDrillingSubSection==10 || prevDrillingSubSection==11 ) {
                          acumHorizontal+=acumMd;
                      }
                      acumMd=0;  
                    }
                    acumMd+=(currMd-prevMd);                    
                }                    
            }
            if (prevDrillingSubSection==5 ) {
               oWS.datos[1][1]=na(acumMd);
            }
            if (prevDrillingSubSection==9 || prevDrillingSubSection==10 || prevDrillingSubSection==11 ) {
                acumHorizontal+=acumMd;
            }
            oWS.datos[2][1]=na(acumHorizontal);
            
            s= "SELECT wellId, drillingSubSectionId, Max(md) AS [max] ";
            s+="FROM ConsultaCorridasPorCriteria1 ";
            s+="GROUP BY wellId, drillingSubSectionId ";
            s+="HAVING wellId=" + wellId + " ";
            s+="ORDER BY Max(ConsultaCorridasPorCriteria1.md);";
            
            rs=oBD.select(s);
            int p=0;
            while (rs.next()) {
                aMd[p][0]=new Double(rs.getLong("drillingSubSectionId"));
                aMd[p][1]=new Double(rs.getDouble("max"));
                p++;
            }
            double mdFrom=0,mdTo=0,deltaMd=0,acumDeltaMd=0, porcentajeHz=0;
            long cantReg=0;
            
            for (int x=0;x<=p-1;x++) {
                if (aMd[x][0]>=9 && aMd[x][0]<=11) {
                    
                    mdFrom=(x==0)?0:aMd[x-1][1];
                    mdTo=aMd[x][1];
                    
                    deltaMd=mdTo-mdFrom;
                    acumDeltaMd+=deltaMd;
                    
                    s= "SELECT Sections.wellId, LASPerMD.dept, LASPerMD.gr ";
                    s+="FROM LASPerMD INNER JOIN (LAS INNER JOIN (Run INNER JOIN Sections ON Run.sectionId = Sections.id) ON LAS.runId = Run.Id) ON LASPerMD.lasId = LAS.id ";
                    s+="WHERE (((Sections.wellId)="+ wellId +") AND ((LASPerMD.dept)>="+mdFrom+" And (LASPerMD.dept)<="+mdTo+") AND ((LASPerMD.gr)>0) AND ((LASPerMD.gr)<=45)) ";
                    s+="ORDER BY LASPerMD.dept;";
                    
                    rs=oBD.select(s);
                    
                    while (rs.next()) {
                        cantReg++;
                    }
                    
                }
            }
            porcentajeHz=((cantReg/2)/(acumDeltaMd))*100;                    

            oWS.datos[3][1]=na(porcentajeHz);
            
            s="SELECT Well.id, Well.nombre, Well.wellTypeId, WellType.nombre as wellTypeNombre ";
            s+="FROM WellType INNER JOIN Well ON WellType.Id = Well.wellTypeId ";
            s+="WHERE Well.id="+wellId;
            
            rs=oBD.select(s);

            if (rs.next()) {
               oWS.datos[4][1]=rs.getString("wellTypeNombre");
            }
            
            int nro=0;
            String s1=null;
            for (int x=1;x<=oWS.infoSecciones.length-1;x++) {
                try {
                s1=oWS.infoSecciones[x][0].toString();
                if (s1=="") break;
                nro=new Integer(s1);
                s="SELECT wellId, sectionsId, sectionsNumeroIdentificador, Min(dls) AS [Min], Max(dls) AS [Max] ";
                s+="FROM ConsultaCorridasPorCriteria1 ";
                s+="GROUP BY wellId, sectionsId, sectionsNumeroIdentificador ";
                s+="HAVING wellId="+wellId+" AND sectionsNumeroIdentificador="+nro+";";  
                rs=oBD.select(s);

                if (rs.next()) {
                   oWS.infoSecciones[x][4]=na(rs.getDouble("Max"));
                   oWS.infoSecciones[x][5]=na(rs.getDouble("Min"));
                } 
                
                } catch (NullPointerException ex) {}
            }
            
            s="SELECT wellId, drillingSubSectionId, drillingSubSeccionDescription, Avg(azim) AS PromedioDeazim " ;
            s+="FROM ConsultaCorridasPorCriteria1 " ;
            s+="GROUP BY wellId, drillingSubSectionId, drillingSubSeccionDescription " ;
            s+="HAVING wellId="+wellId+" ORDER BY drillingSubSectionId ASC;";
            
            rs=oBD.select(s);
            double prevAzim,currAzim;
            firstTime=true;
            Double[][] azim=new Double[10][2];
            int x=0,x1=0;
            while (rs.next()) {
                azim[x][0]=new Double(rs.getLong("drillingSubSectionId"));
                azim[x][1]=rs.getDouble("PromedioDeazim");
                if(azim[x][0]==5) x1=x;
                x++;
            }
            if (x1>0) {
                oWS.datos[5][1]=na(azim[x1-1][1]);
                oWS.datos[6][1]=na(azim[x1+1][1]);
            }
            
            s="SELECT Well.id, Sections.id, Sections.numeroIdentificador, Run.Id, Run.numero, Survey.tort, Survey.ddi, Survey.erd " ;
            s+="FROM Survey INNER JOIN (Run INNER JOIN (Sections INNER JOIN Well ON Sections.wellId = Well.id) ON Run.sectionId = Sections.id) ON Survey.runId = Run.Id " ;
            s+="WHERE Well.id="+wellId + " ";
            s+="ORDER BY Well.id, Sections.id DESC , Run.Id DESC;";
            
            rs=oBD.select(s);
            
            if (rs.next()) {
                oWS.datos[7][1]=na(rs.getDouble("ddi"));
                oWS.datos[8][1]=na(rs.getDouble("erd"));
                oWS.datos[9][1]=na(rs.getDouble("tort"));
            }                      
            
        } catch (SQLException ex) {
            return null;
        }
        
        return oWS;
    }
    
    private void exportarTVD() {        
        TvdDetail oTD=null;
        TvdDetail[][] aTD=null;
        oTD=new TvdDetail();
        String s="";
        ResultSet rs=null;
        int i=0;
        String archivo="";
        File currDirectory;
        currDirectory=new File(System.getProperty("user.home"));
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(currDirectory);
        fileChooser.setDialogTitle("Escoja el directorio/carpeta");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos Excel", new String[] {"xls", "xlsx"});
        fileChooser.setFileFilter(filter);
        int result= fileChooser.showSaveDialog(this);
        File fn=fileChooser.getSelectedFile();
        try {
            archivo=fn.getPath()+".xls";
        } catch (Exception e){return;}
        cursorEspera();
        if (wellId>0) {
            aTD=this.armarTvdDetail(wellId);
            crearArchivo_TvdDetail(aTD,archivo);
            cursorNormal();
            return;
        }       
    }
       
    private void exportarSummary() {
        
        WellSummary oWS=null;
        WellSummary[] aWS=null;
        oWS=new WellSummary();
        String s="";
        ResultSet rs=null;
        int i=0;
        String archivo="";
        File currDirectory;
        currDirectory=new File(System.getProperty("user.home"));
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(currDirectory);
        fileChooser.setDialogTitle("Escoja el directorio/carpeta");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos Excel", new String[] {"xls", "xlsx"});
        fileChooser.setFileFilter(filter);
        int result= fileChooser.showSaveDialog(this);
        File fn=fileChooser.getSelectedFile();
        try {
            archivo=fn.getPath()+".xls";
        } catch (NullPointerException ex) {return;} 
        cursorEspera();
        if (wellId>0) {
            aWS=new WellSummary[1];
            oWS=this.armarInfoPozo(wellId);
            aWS[0]=oWS;
            crearArchivo_Summary(aWS,archivo); 
            cursorNormal();
            return;
        }
        if (macollaId>0) {
           s="SELECT Well.macollaId, Well.id as WellId FROM Well " ;
           s+="WHERE Well.macollaId="+macollaId+" ";
           s+="ORDER BY Well.id;";
           rs=oBD.select(s);
           try {
            i=0;
            while (rs.next()) {
                i++;
            }
            if (i>0) {
               aWS=new WellSummary[i];
               rs.beforeFirst();
               i=0;
               while (rs.next()) {
                 oWS=this.armarInfoPozo(rs.getLong("WellId"));
                 aWS[i]=oWS;
                 i++;
               }
            }
           } catch (SQLException ex) {} 
           crearArchivo_Summary(aWS,archivo); 
           cursorNormal();
           return;
        }
        if (campoId>0) {
           s="SELECT Well.id as WellId, CampoCliente.campoId " ;
           s+="FROM (Well INNER JOIN Macolla ON Well.macollaId = Macolla.id) INNER JOIN CampoCliente ON Macolla.campoClienteId = CampoCliente.id ";
           s+="WHERE CampoCliente.campoId="+campoId+" " ;
           s+="ORDER BY Well.id;";
           rs=oBD.select(s);
           try {
            i=0;
            while (rs.next()) {
                i++;
            }
            if (i>0) {
               aWS=new WellSummary[i];
               rs.beforeFirst();
               i=0;
               while (rs.next()) {
                 oWS=this.armarInfoPozo(rs.getLong("WellId"));
                 aWS[i]=oWS;
                 i++;
               }
            }
           } catch (SQLException ex) {}  
           crearArchivo_Summary(aWS,archivo); 
           cursorNormal();
           return;
        }
        if (clienteId>0) {
           s="SELECT Well.id as WellId, CampoCliente.clienteId " ;
           s+="FROM (Well INNER JOIN Macolla ON Well.macollaId = Macolla.id) INNER JOIN CampoCliente ON Macolla.campoClienteId = CampoCliente.id ";
           s+="WHERE CampoCliente.clienteId="+clienteId+" " ;
           s+="ORDER BY Well.id;";
           rs=oBD.select(s);
           try {
            i=0;
            while (rs.next()) {
                i++;
            }
            if (i>0) {
               aWS=new WellSummary[i];
               rs.beforeFirst();
               i=0;
               while (rs.next()) {
                 oWS=this.armarInfoPozo(rs.getLong("WellId"));
                 aWS[i]=oWS;
                 i++;
               }
            }
           } catch (SQLException ex) {}
           crearArchivo_Summary(aWS,archivo); 
           cursorNormal();
           return;
        }
        cursorNormal();
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
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jRadioButtonCliente = new javax.swing.JRadioButton();
        jRadioButtonCampo = new javax.swing.JRadioButton();
        jRadioButtonMacolla = new javax.swing.JRadioButton();
        jComboBoxClientes = new javax.swing.JComboBox();
        jComboBoxCampo = new javax.swing.JComboBox();
        jComboBoxMacolla = new javax.swing.JComboBox();
        jTextFieldBuscar = new javax.swing.JTextField();
        jLabelPozo = new javax.swing.JLabel();
        jButtonBuscar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListPozos = new javax.swing.JList();
        jRadioButtonDummy = new javax.swing.JRadioButton();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPaneLeccionesAprendidas = new javax.swing.JTextPane();
        jLabelLongitud2 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTableSecciones = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTableDatos = new javax.swing.JTable();
        jLabelGraficar = new javax.swing.JLabel();
        jLabelExportarSummary = new javax.swing.JLabel();
        jLabelExportarTVD = new javax.swing.JLabel();

        setTitle("Well Summary");
        setMinimumSize(new java.awt.Dimension(780, 550));
        setModal(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Criteria de seleccion:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, -1, -1));

        buttonGroup1.add(jRadioButtonCliente);
        jRadioButtonCliente.setText("Cliente");
        jRadioButtonCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonClienteActionPerformed(evt);
            }
        });
        jPanel1.add(jRadioButtonCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        buttonGroup1.add(jRadioButtonCampo);
        jRadioButtonCampo.setText("Campo");
        jRadioButtonCampo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonCampoActionPerformed(evt);
            }
        });
        jPanel1.add(jRadioButtonCampo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, -1, -1));

        buttonGroup1.add(jRadioButtonMacolla);
        jRadioButtonMacolla.setText("Macolla");
        jRadioButtonMacolla.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMacollaActionPerformed(evt);
            }
        });
        jPanel1.add(jRadioButtonMacolla, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, -1));

        jComboBoxClientes.setEnabled(false);
        jComboBoxClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxClientesActionPerformed(evt);
            }
        });
        jPanel1.add(jComboBoxClientes, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 10, 230, -1));

        jComboBoxCampo.setEnabled(false);
        jComboBoxCampo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxCampoActionPerformed(evt);
            }
        });
        jPanel1.add(jComboBoxCampo, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 40, 230, -1));

        jComboBoxMacolla.setEnabled(false);
        jComboBoxMacolla.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxMacollaActionPerformed(evt);
            }
        });
        jPanel1.add(jComboBoxMacolla, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 70, 230, -1));
        jPanel1.add(jTextFieldBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 10, 140, 30));

        jLabelPozo.setText("Pozo:");
        jLabelPozo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.add(jLabelPozo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 370, -1));

        jButtonBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/my/lupa.png"))); // NOI18N
        jButtonBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBuscarActionPerformed(evt);
            }
        });
        jPanel1.add(jButtonBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 10, 30, 30));

        jListPozos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListPozosMouseClicked(evt);
            }
        });
        jListPozos.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListPozosValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jListPozos);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 50, 180, 70));

        buttonGroup1.add(jRadioButtonDummy);
        jRadioButtonDummy.setText("dummy");
        jPanel1.add(jRadioButtonDummy, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 40, 20, -1));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 730, 130));

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/my/SB.png"))); // NOI18N
        jLabel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel9MouseClicked(evt);
            }
        });
        getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 460, 140, 40));

        jTextPaneLeccionesAprendidas.setEditable(false);
        jTextPaneLeccionesAprendidas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextPaneLeccionesAprendidasKeyTyped(evt);
            }
        });
        jScrollPane2.setViewportView(jTextPaneLeccionesAprendidas);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, 310, 280));

        jLabelLongitud2.setText("Lecciones Aprendidas:");
        getContentPane().add(jLabelLongitud2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, -1, -1));

        jTableSecciones.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Seccion #", "long/md", "corridas", "brt", "dls Max", "dls Min"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(jTableSecciones);

        getContentPane().add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 170, 400, 90));

        jTableDatos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Descripción", "Valor"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                true, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(jTableDatos);

        getContentPane().add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 270, 310, 180));

        jLabelGraficar.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelGraficar.setText("Graficas de TVD");
        jLabelGraficar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabelGraficar.setEnabled(false);
        jLabelGraficar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelGraficarMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabelGraficarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelGraficarMouseExited(evt);
            }
        });
        getContentPane().add(jLabelGraficar, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 460, 100, -1));

        jLabelExportarSummary.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelExportarSummary.setText("Exportar Summary");
        jLabelExportarSummary.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabelExportarSummary.setEnabled(false);
        jLabelExportarSummary.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelExportarSummaryMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabelExportarSummaryMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelExportarSummaryMouseExited(evt);
            }
        });
        getContentPane().add(jLabelExportarSummary, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 460, 120, -1));

        jLabelExportarTVD.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelExportarTVD.setText("Exportar TVD/Sección");
        jLabelExportarTVD.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabelExportarTVD.setEnabled(false);
        jLabelExportarTVD.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelExportarTVDMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabelExportarTVDMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelExportarTVDMouseExited(evt);
            }
        });
        getContentPane().add(jLabelExportarTVD, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 460, 140, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jRadioButtonClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonClienteActionPerformed
        prepararControlesDeCriteria();
    }//GEN-LAST:event_jRadioButtonClienteActionPerformed

    private void jRadioButtonCampoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonCampoActionPerformed
        prepararControlesDeCriteria();
    }//GEN-LAST:event_jRadioButtonCampoActionPerformed

    private void jRadioButtonMacollaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMacollaActionPerformed
        prepararControlesDeCriteria();
    }//GEN-LAST:event_jRadioButtonMacollaActionPerformed

    private void jComboBoxClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxClientesActionPerformed
        clienteId=oManejoDeCombos.getComboID(this.jComboBoxClientes);
        String s="SELECT campoId,campoNombre from ConsultaCampoCliente1 WHERE clienteId="+clienteId;
        oManejoDeCombos.llenaCombo(oBD,oManejoDeCombos.getModeloCombo(),s,this.jComboBoxCampo,"Seleccione Campo");
    }//GEN-LAST:event_jComboBoxClientesActionPerformed

    private void jComboBoxCampoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxCampoActionPerformed
        clienteId=oManejoDeCombos.getComboID(this.jComboBoxClientes);
        campoId=oManejoDeCombos.getComboID(this.jComboBoxCampo);
        String s="SELECT macollaId,macollaNombre from ConsultaMacolla1 WHERE clienteId="+clienteId + " AND campoId="+campoId;
        oManejoDeCombos.llenaCombo(oBD,oManejoDeCombos.getModeloCombo(),s,this.jComboBoxMacolla,"Seleccione Macolla");
    }//GEN-LAST:event_jComboBoxCampoActionPerformed

    private void jComboBoxMacollaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxMacollaActionPerformed
        macollaId=oManejoDeCombos.getComboID(this.jComboBoxMacolla);
    }//GEN-LAST:event_jComboBoxMacollaActionPerformed

    private void jLabel9MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MouseClicked
        msgbox(MainForm.msgVersion,"About WellDataReport");
    }//GEN-LAST:event_jLabel9MouseClicked

    private void jButtonBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBuscarActionPerformed
        buscarPozo();
    }//GEN-LAST:event_jButtonBuscarActionPerformed

    private void jListPozosValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListPozosValueChanged
        accionClickLista();
    }//GEN-LAST:event_jListPozosValueChanged

    private void jListPozosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListPozosMouseClicked
        accionClickLista();
    }//GEN-LAST:event_jListPozosMouseClicked

    private void jTextPaneLeccionesAprendidasKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextPaneLeccionesAprendidasKeyTyped
    }//GEN-LAST:event_jTextPaneLeccionesAprendidasKeyTyped

    private void jLabelGraficarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelGraficarMouseEntered
        jLabelGraficar.setForeground(Color.BLUE);
    }//GEN-LAST:event_jLabelGraficarMouseEntered

    private void jLabelGraficarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelGraficarMouseExited
        jLabelGraficar.setForeground(Color.BLACK);
    }//GEN-LAST:event_jLabelGraficarMouseExited

    private void jLabelGraficarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelGraficarMouseClicked
        prepararPloteosyGraficar();
    }//GEN-LAST:event_jLabelGraficarMouseClicked

    private void jLabelExportarSummaryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelExportarSummaryMouseClicked
        exportarSummary();
    }//GEN-LAST:event_jLabelExportarSummaryMouseClicked

    private void jLabelExportarSummaryMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelExportarSummaryMouseEntered
        jLabelExportarSummary.setForeground(Color.BLUE);
    }//GEN-LAST:event_jLabelExportarSummaryMouseEntered

    private void jLabelExportarSummaryMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelExportarSummaryMouseExited
        jLabelExportarSummary.setForeground(Color.BLACK);
    }//GEN-LAST:event_jLabelExportarSummaryMouseExited

    private void jLabelExportarTVDMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelExportarTVDMouseClicked
        exportarTVD();
    }//GEN-LAST:event_jLabelExportarTVDMouseClicked

    private void jLabelExportarTVDMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelExportarTVDMouseEntered
        jLabelExportarTVD.setForeground(Color.BLUE);
    }//GEN-LAST:event_jLabelExportarTVDMouseEntered

    private void jLabelExportarTVDMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelExportarTVDMouseExited
        jLabelExportarTVD.setForeground(Color.BLACK);
    }//GEN-LAST:event_jLabelExportarTVDMouseExited
    public void cursorNormal(){
        this.setCursor(Cursor.getDefaultCursor());
    }
    
    public void cursorEspera(){
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
    
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
            java.util.logging.Logger.getLogger(Rpt003.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Rpt003.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Rpt003.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Rpt003.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Rpt003 dialog = new Rpt003(new javax.swing.JFrame(), true,new ManejoBDAccess());
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
    private javax.swing.JButton jButtonBuscar;
    private javax.swing.JComboBox jComboBoxCampo;
    private javax.swing.JComboBox jComboBoxClientes;
    private javax.swing.JComboBox jComboBoxMacolla;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelExportarSummary;
    private javax.swing.JLabel jLabelExportarTVD;
    private javax.swing.JLabel jLabelGraficar;
    private javax.swing.JLabel jLabelLongitud2;
    private javax.swing.JLabel jLabelPozo;
    private javax.swing.JList jListPozos;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButtonCampo;
    private javax.swing.JRadioButton jRadioButtonCliente;
    private javax.swing.JRadioButton jRadioButtonDummy;
    private javax.swing.JRadioButton jRadioButtonMacolla;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTableDatos;
    private javax.swing.JTable jTableSecciones;
    private javax.swing.JTextField jTextFieldBuscar;
    private javax.swing.JTextPane jTextPaneLeccionesAprendidas;
    // End of variables declaration//GEN-END:variables
}

class WellSummary {
    long wellId;
    String wellNombre;
    String macollaNombre;
    String campoNombre;
    String clienteNombre;
    String divisionNombre;
    String leccionesAprendidas;
    Object[][] infoSecciones=new Object[10][6];
    Object[][] datos=new Object[10][2];    
}

class TvdDetail {
    long wellId;
    String wellNombre;
    double tvd;
    double md;
    double gr;
    double dls;
    long seccionId;
    long seccionNumeroIdentificador;
    double porcentajeSliding;
    double porcentajeSteering;
    double dlsPorDeltaMdSliding;
    double dlsPorDeltaMdSteering;
    double porcentajeArena;
    String clasificacion; 
    String drillingSubSectionType;
}
