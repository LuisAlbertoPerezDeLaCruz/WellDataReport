/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import static miLibreria.GlobalConstants.valorNulo;
import static miLibreria.GlobalConstants.valorNuloMuestra;
import miLibreria.ManejoDeCombos;
import miLibreria.bd.*;
import miLibreria.Numeros;
import my.loadWellData.MantBHA;

import org.jfree.chart.ChartPanel; 
import org.jfree.chart.JFreeChart; 
import org.jfree.data.xy.XYDataset; 
import org.jfree.data.xy.XYSeries; 
import org.jfree.chart.ChartFactory; 
import org.jfree.chart.plot.PlotOrientation; 
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeriesCollection; 



/**
 *
 * @author Luis
 */
public class Rpt001 extends javax.swing.JDialog {
    public miLibreria.bd.ManejoBDI oBD;
    private ManejoDeCombos oManejoDeCombos;
    public double factorDesde, factorHasta;
    public double tvdDesde=0.0, tvdHasta=0.0;
    public long clienteId=0,campoId=0, macollaId=0,sectionSubTypeId=0;
    public Long[] aCorridas;
    public int cantCorridas;
    public Numeros oNumeros=new Numeros();
    public MyRendererGr myRenderer;
    public int rowsInLas=0;
    private JDialog Ventana;
    private boolean esRS=false;    
    DefaultTableModel modeloCorridas, modeloSlideSheet, modeloLAS;
    private Calculos oCalc=null;
    

    
    public Rpt001(java.awt.Frame parent, boolean modal,ManejoBDI o ) {
        super(parent, modal);
        initComponents();
        oManejoDeCombos = new ManejoDeCombos();
        oBD=o;
        oManejoDeCombos.llenaCombo(oBD,oManejoDeCombos.getModeloCombo(),DrillingSubSectionType.class,this.jComboBoxDrillingSubType,"Seleccione SubSectionType");
        seteosIniciales(); 
        modeloCorridas=(DefaultTableModel) this.jTableCorridas.getModel();
        modeloSlideSheet=(DefaultTableModel) this.jTableSlideSheet.getModel();
        modeloLAS=(DefaultTableModel) this.jTableLAS.getModel();
        estadoDistanciaCtrls();
    }
    
    
    public void seteosIniciales() {
        int tiempoEnMilisegundos = 500;
        Timer timer = new Timer (tiempoEnMilisegundos, new ActionListener () { 
        public void actionPerformed(ActionEvent e) {
            boolean ok=true;
            factorDesde=0.0;
            factorHasta=0.0;
            if (Rpt001.this.jRadioButtonTr.isSelected()) {
                Rpt001.this.jComboBoxInclinacion.setVisible(true);
            } else {
                Rpt001.this.jComboBoxInclinacion.setVisible(false);
            }
            if (jTextFieldDlsDesde.getText().length()>0) {
                if (jTextFieldDlsDesde.getText().matches("-?\\d+(\\.\\d+)?")) {
                 factorDesde=oNumeros.valorDouble(jTextFieldDlsDesde.getText());                 
                }
            }
            else ok=false;
            if (jTextFieldDlsHasta.getText().length()>0) {
                if (jTextFieldDlsHasta.getText().matches("-?\\d+(\\.\\d+)?")) {
                 factorHasta=oNumeros.valorDouble(jTextFieldDlsHasta.getText());                 
                }
            }
            else ok=false;

            if (factorDesde<factorHasta) {
            } else {
                ok=false; 
            }            
            if (clienteId+campoId+macollaId==0) ok=false;
            
            if (Rpt001.this.jRadioButtonCliente.isSelected())  {
                if (clienteId==0) ok=false;
            }
            if (Rpt001.this.jRadioButtonCampo.isSelected())  {
                if (clienteId==0) ok=false;
                if (campoId==0) ok=false;
            }
            if (Rpt001.this.jRadioButtonMacolla.isSelected())  {
                if (clienteId==0) ok=false;
                if (campoId==0) ok=false;
                if (macollaId==0) ok=false;                 
            }
            if (sectionSubTypeId==0) ok=false;
            
            jButtonBuscarCorridas.setEnabled(ok);
            
            if (Rpt001.this.jTextFieldTVDDesde.getText().isEmpty()==false)
                tvdDesde=Double.parseDouble(Rpt001.this.jTextFieldTVDDesde.getText());
            if (Rpt001.this.jTextFieldTVDHasta.getText().isEmpty()==false)
                tvdHasta=Double.parseDouble(Rpt001.this.jTextFieldTVDHasta.getText());
            
            //Añadido para el filtro del TVD
            if (aCorridas==null){
                Rpt001.this.jTextFieldTVDDesde.setEnabled(false);
                Rpt001.this.jTextFieldTVDHasta.setEnabled(false);
                Rpt001.this.jButtonFiltrar.setEnabled(false);
                Rpt001.this.jTableCorridas.setEnabled(false);
            } else {
                if (aCorridas.length>0){
                    Rpt001.this.jTextFieldTVDDesde.setEnabled(true);
                    Rpt001.this.jTextFieldTVDHasta.setEnabled(true);
                    Rpt001.this.jTableCorridas.setEnabled(true);
                    if (tvdDesde >0 && tvdHasta >0)
                        Rpt001.this.jButtonFiltrar.setEnabled(true);
                    else
                        Rpt001.this.jButtonFiltrar.setEnabled(false);                        
                }                
            } 
            estadoDistanciaCtrls();
            pack();
        } 
        });
        timer.start();
    }
    
    private void estadoDistanciaCtrls(){
        boolean ok=false;
        double longitud=0.0, latitud=0.0;
        if (this.jToggleButtonDistance.isEnabled() && this.jToggleButtonDistance.isSelected()){
            ok=true;
        }
        this.jLabelLatitud.setVisible(ok);
        this.jLabelLongitud.setVisible(ok);
        this.jLabelDistancia.setVisible(ok);
        this.jTextFieldLatitud.setVisible(ok);
        this.jTextFieldLongitud.setVisible(ok);
        this.jButtonCalcular.setVisible(ok);
        this.jLabelOrigen.setVisible(ok);
        this.jButtonCalcular.setEnabled(false);
        
        if (this.jTextFieldLongitud.getText().isEmpty()==false)
            longitud=Double.parseDouble(this.jTextFieldLongitud.getText());
        if (this.jTextFieldLatitud.getText().isEmpty()==false)
            latitud=Double.parseDouble(this.jTextFieldLatitud.getText());
        if (longitud>0 && latitud>0)
            this.jButtonCalcular.setEnabled(true);
    }
    
    private void buscarCorridas() {
        String key="";
        String s;
        ResultSet rs;
        int i=0;
        limpiarTablaCorridas();
        limpiarTablaSurvey();
        limpiarTablaPlan();
        limpiarTablaSlideSheet();
        limpiarTablaLAS();
        if (this.jRadioButtonDls.isSelected()) {
           key="dls" ;
        }
        if (this.jRadioButtonBr.isSelected()) {
           key="Br" ;
        }
        if (this.jRadioButtonTr.isSelected()) {
           key="Tr" ;
        }
        s="SELECT DISTINCT clientesNombre, campoNombre, macollaNombre, wellNombre, sectionsNumeroIdentificador, runNumero, runId, md, tvd ";
        s+="FROM ConsultaCorridasPorCriteria1 WHERE "+key+" >=" + this.factorDesde + " AND "+key+"<=" + this.factorHasta ;
        s+=" AND clientesId="+clienteId + " AND drillingSubSectionId=" + this.sectionSubTypeId;
        if (key=="Tr") {
           switch (this.jComboBoxInclinacion.getSelectedIndex()) {
               case 0:
                   s+=" AND incl>0 AND incl<=29.5";
                   break;
               case 1:
                   s+=" AND incl>29.5 AND incl<=59.5";
                   break;
               case 2:
                   s+=" AND incl>59.5 AND incl<=74.5";
                   break;
               case 3:
                   s+=" AND incl>74.5 AND incl<=100";
                   break;
           }            
        }
        if (campoId>0) {
            s+=" AND campoId="+campoId;    
        }
        if (macollaId>0) {
            s+=" AND macollaId="+macollaId;    
        }        
        s+=";";       
        rs=oBD.select(s);
        try {
            while (rs.next()) {                
                i++;
            }
            cantCorridas=i;
            aCorridas = new Long[i];
            i=0;
            rs.beforeFirst();
            while (rs.next()) {                
                modeloCorridas.setRowCount(i+1);
                this.jTableCorridas.setValueAt(rs.getString("campoNombre"), i, 0); 
                this.jTableCorridas.setValueAt(rs.getString("macollaNombre"), i, 1);
                this.jTableCorridas.setValueAt(rs.getString("wellNombre"), i, 2);
                this.jTableCorridas.setValueAt(rs.getLong("sectionsNumeroIdentificador"), i, 3);
                this.jTableCorridas.setValueAt(rs.getLong("runNumero"), i, 4);
                this.jTableCorridas.setValueAt(na(rs.getDouble("md")), i, 5);
                this.jTableCorridas.setValueAt(na(rs.getDouble("tvd")), i, 6);
                aCorridas[i]=rs.getLong("runId");
                i++;
            }
            this.jScrollPaneCorridas.getVerticalScrollBar().setValue(0);
            

        } catch (SQLException ex) {
            Logger.getLogger(Rpt001.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.jTableCorridas.setEnabled(true);
        this.jLabelMostrarBHA.setEnabled(false);
        this.jToggleButtonDistance.setEnabled(false);
        this.jLabelGrafica.setEnabled(false);
        this.jLabelSlideSheet.setText("Slide Sheet");
    }
    
    private void limpiarTablaCorridas() {
        for (int i=0;i<=this.jTableCorridas.getRowCount()-1;i++) {
            for (int j=0;j<=this.jTableCorridas.getColumnCount()-1;j++) {
                this.jTableCorridas.setValueAt("", i, j);                
            }
        }
        aCorridas=null;
        this.jTableCorridas.clearSelection();
        limpiarTablaSurvey();
        limpiarTablaPlan();
        limpiarTablaSlideSheet();
        limpiarTablaLAS();
    }
    
    private void limpiarTablaSurvey() {
        for (int i=0;i<=jTableSurvey.getRowCount()-1;i++) {
            for (int j=0;j<=jTableSurvey.getColumnCount()-1;j++) {
                this.jTableSurvey.setValueAt("", i, j);
            }
        }
    }
    
    private void limpiarTablaPlan() {
        for (int i=0;i<=jTablePlan.getRowCount()-1;i++) {
            for (int j=0;j<=jTablePlan.getColumnCount()-1;j++) {
                this.jTablePlan.setValueAt("", i, j);
            }
        }
    }
    
    private void limpiarTablaSlideSheet() {
        for (int i=0;i<=jTableSlideSheet.getRowCount()-1;i++) {
            for (int j=0;j<=jTableSlideSheet.getColumnCount()-1;j++) {
                this.jTableSlideSheet.setValueAt("", i, j);
            }
        }
    }
    
    private void limpiarTablaLAS() {
        for (int i=0;i<=jTableLAS.getRowCount()-1;i++) {
            for (int j=0;j<=jTableLAS.getColumnCount()-1;j++) {
                this.jTableLAS.setValueAt("", i, j);
            }
        }
    }  
    
    private void mostrarInfo() {
        esRS=esRS();
        mostrarSurvey();
        mostrarPlan();
        mostrarSlideSheet();
        mostrarLAS();
        this.jLabelMostrarBHA.setEnabled(true);
        this.jToggleButtonDistance.setEnabled(true);
        this.jLabelGrafica.setEnabled(true);
    }
    
    private void  mostrarBHA() {
        my.loadWellData.MantBHA j=new MantBHA(new javax.swing.JFrame(), true);
        j.setLocationRelativeTo(this);
        int p=this.jTableCorridas.getSelectedRow();
        long runId=aCorridas[p];
        Run oRun=new Run();
        try {
            oRun=(Run) oBD.select(Run.class, "Id="+runId)[0];
            j.sDescNodo="Run#:"+oRun.getNumero();
            j.oRunFromMainForm=oRun;
            j.allowEdit=false;
            j.setParam(oBD);
            j.setVisible(true);
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Rpt001.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean esRS() {
        boolean es=false;
                
        int p=this.jTableCorridas.getSelectedRow();
        long runId=aCorridas[p];
        BHA oBHA=new BHA();
        try {
            oBHA=(BHA) oBD.select(BHA.class,"runID="+runId)[0];
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Rpt001.class.getName()).log(Level.SEVERE, null, ex);
        }
        if ("RSS".equals(oBHA.getTipoDT())) es=true;
        return es;
    }
    
    private void mostrarSurvey() {
       Survey oSurvey=new Survey();
       SurveyPerMD oSurveyPerMd=new SurveyPerMD();
       Object[] o;
       int p=this.jTableCorridas.getSelectedRow();
       long runId=aCorridas[p];
       long surveyId=0;
       Double md=0.0;
       final int actual=1,anterior=0;
       DecimalFormat df = new DecimalFormat("##########0.000");
       
       limpiarTablaSurvey();
       md=new Double(jTableCorridas.getValueAt(p, 5).toString());
       try {
            oSurvey=(Survey) oBD.select(Survey.class, "runId="+runId)[0];
            surveyId=oSurvey.getId();
            o=oBD.select(SurveyPerMD.class, "surveyId="+surveyId);
            for (int i=0;i<=o.length-1;i++) {
                oSurveyPerMd=(SurveyPerMD) o[i];
                if (MainForm.mdIguales(oSurveyPerMd.getMd(),md)) {
                    jTableSurvey.setValueAt(na(oSurveyPerMd.getMd()),actual,0);
                    jTableSurvey.setValueAt(na(oSurveyPerMd.getIncl()),actual,1);                    
                    jTableSurvey.setValueAt(na(oSurveyPerMd.getAzim()),actual,2);   
                    jTableSurvey.setValueAt(na(oSurveyPerMd.getTvd()),actual,3);
                    jTableSurvey.setValueAt(na(oSurveyPerMd.getVsec()),actual,4);
                    jTableSurvey.setValueAt(na(oSurveyPerMd.getNs()),actual,5);
                    jTableSurvey.setValueAt(na(oSurveyPerMd.getEw()),actual,6);
                    jTableSurvey.setValueAt(na(oSurveyPerMd.getDls()),actual,7);
                    jTableSurvey.setValueAt(na(oSurveyPerMd.getTr()),actual,8);
                    jTableSurvey.setValueAt(na(oSurveyPerMd.getBr()),actual,9);
                    if ((i-1)>=0) oSurveyPerMd=(SurveyPerMD) o[i-1];
                    jTableSurvey.setValueAt(na(oSurveyPerMd.getMd()),anterior,0);
                    jTableSurvey.setValueAt(na(oSurveyPerMd.getIncl()),anterior,1);                    
                    jTableSurvey.setValueAt(na(oSurveyPerMd.getAzim()),anterior,2);   
                    jTableSurvey.setValueAt(na(oSurveyPerMd.getTvd()),anterior,3);
                    jTableSurvey.setValueAt(na(oSurveyPerMd.getVsec()),anterior,4);
                    jTableSurvey.setValueAt(na(oSurveyPerMd.getNs()),anterior,5);
                    jTableSurvey.setValueAt(na(oSurveyPerMd.getEw()),anterior,6);
                    jTableSurvey.setValueAt(na(oSurveyPerMd.getDls()),anterior,7);
                    jTableSurvey.setValueAt(na(oSurveyPerMd.getTr()),anterior,8);
                    jTableSurvey.setValueAt(na(oSurveyPerMd.getBr()),anterior,9);                    
                }
            }
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Rpt001.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    private void mostrarPlan() {
       WellPlan oWellPlan=new WellPlan();
       WellPlanPerMD oPlanPerMd=new WellPlanPerMD();
       Object[] o;
       int p=this.jTableCorridas.getSelectedRow();
       long runId=aCorridas[p];
       long wellPlanId=0;
       Double md=0.0;
       Double md2=0.0;
       final int actual=0;       
       limpiarTablaPlan();
       md=new Double(jTableCorridas.getValueAt(p, 5).toString());
       
       try { 
            if (oBD.select(WellPlan.class, "runId="+runId).length>0) {
                oWellPlan=(WellPlan) oBD.select(WellPlan.class, "runId="+runId)[0];
                wellPlanId=oWellPlan.getId();
                o=oBD.select(WellPlanPerMD.class, "wellPlanId="+wellPlanId);
                for (int i=0;i<=o.length-1;i++) {
                    oPlanPerMd=(WellPlanPerMD) o[i];
                    md2=oPlanPerMd.getMd();
                    if (md2.longValue()==md.longValue()) {
                        jTablePlan.setValueAt(na(oPlanPerMd.getMd()),actual,0);
                        jTablePlan.setValueAt(na(oPlanPerMd.getIncl()),actual,1);                    
                        jTablePlan.setValueAt(na(oPlanPerMd.getAzim()),actual,2);   
                        jTablePlan.setValueAt(na(oPlanPerMd.getTvd()),actual,3);
                        jTablePlan.setValueAt(na(oPlanPerMd.getVsec()),actual,4);
                        jTablePlan.setValueAt(na(oPlanPerMd.getNs()),actual,5);
                        jTablePlan.setValueAt(na(oPlanPerMd.getEw()),actual,6);
                        jTablePlan.setValueAt(na(oPlanPerMd.getDls()),actual,7);
                        jTablePlan.setValueAt(na(oPlanPerMd.getTr()),actual,8); 
                        jTablePlan.setValueAt(na(oPlanPerMd.getBr()),actual,9); 
                    }
                }                
            }
        } catch (Exception ex ) {
            Logger.getLogger(Rpt001.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    private void mostrarSlideSheet() {
       SlideSheet oSlideSheet=new SlideSheet();
       SlideSheetPerMD oSlideSheetPerMd=new SlideSheetPerMD();
       Object[] o;
       int p=this.jTableCorridas.getSelectedRow();
       long runId=aCorridas[p];
       long slideSheetId=0;
       Double md1=0.0,md2=0.0, deltaMd=0.0, sumatoriaDeltaMd=0.0,
          sumatoriaSliding=0.0, porcentajeSliding=0.0, differential=0.0;
       Double sumatoriaDeltaMdPorDesiredPowerSetting=0.0;
       Double porcentajeSteering=0.0;
       int row=0;       
       limpiarTablaSlideSheet();
       if (!"".equals(jTableSurvey.getValueAt(0, 0).toString().trim()))
        md1=new Double(jTableSurvey.getValueAt(0, 0).toString());
       if (!"".equals(jTableSurvey.getValueAt(1, 0).toString().trim()))
        md2=new Double(jTableSurvey.getValueAt(1, 0).toString());   
       this.jLabelSlideSheet.setText("Slide Sheet");
//       if (esRS) this.jTableSlideSheet.getColumnModel().getColumn(6).setHeaderValue("operation mode");
//       else this.jTableSlideSheet.getColumnModel().getColumn(6).setHeaderValue("drilling mode");
       try {
            oSlideSheet=(SlideSheet) oBD.select(SlideSheet.class, "runId="+runId)[0];
            slideSheetId=oSlideSheet.getId();
            o=oBD.select(SlideSheetPerMD.class, "slideSheetId="+slideSheetId);
            for (int i=0;i<=o.length-1;i++) {
                oSlideSheetPerMd=(SlideSheetPerMD) o[i];
                  if (oSlideSheetPerMd.getMdTo()>=md1 && oSlideSheetPerMd.getMdFrom()<=md2 ) {
                    modeloSlideSheet.setRowCount(row+1);
                    jTableSlideSheet.setValueAt(new SimpleDateFormat("dd/MM/yyyy").format(oSlideSheetPerMd.getFecha()),row,0);
                    jTableSlideSheet.setValueAt(new SimpleDateFormat("hh:mm:ss").format(oSlideSheetPerMd.getStartTime()),row,1);                    
                    jTableSlideSheet.setValueAt(new SimpleDateFormat("hh:mm:ss").format(oSlideSheetPerMd.getEndTime()),row,2);                    
                    if (row==0) {
                        jTableSlideSheet.setValueAt(na(md1),row,3);
                    }
                    else {
                        jTableSlideSheet.setValueAt(na(oSlideSheetPerMd.getMdFrom()),row,3);
                    }                    
                    jTableSlideSheet.setValueAt(na(oSlideSheetPerMd.getMdTo()),row,4);                    
                    deltaMd=Double.parseDouble(jTableSlideSheet.getValueAt(row,4).toString())-Double.parseDouble(jTableSlideSheet.getValueAt(row,3).toString());
                    jTableSlideSheet.setValueAt(na(deltaMd),row,5);
//                    if (esRS) jTableSlideSheet.setValueAt(na(oSlideSheetPerMd.getOperationMode()),row,6);
//                    else jTableSlideSheet.setValueAt(na(oSlideSheetPerMd.getDrillingMode()),row,6);
                    if (!oSlideSheetPerMd.getDrillingMode().trim().isEmpty()) {
                       jTableSlideSheet.setValueAt(na(oSlideSheetPerMd.getDrillingMode()),row,6); 
                    }
                    if (!oSlideSheetPerMd.getOperationMode().trim().isEmpty()) {
                       jTableSlideSheet.setValueAt(na(oSlideSheetPerMd.getOperationMode()),row,6); 
                    }                    
                    jTableSlideSheet.setValueAt(na(oSlideSheetPerMd.getTfMode()),row,7);
                    jTableSlideSheet.setValueAt(na(oSlideSheetPerMd.getTfAngle()),row,8);
                    jTableSlideSheet.setValueAt(na(oSlideSheetPerMd.getFlow()),row,9);  
                    jTableSlideSheet.setValueAt(na(oSlideSheetPerMd.getSppOffBott()),row,10); 
                    jTableSlideSheet.setValueAt(na(oSlideSheetPerMd.getSppOnBott()),row,11);
                    if (jTableSlideSheet.getValueAt(row,11).toString()!="N/A" && jTableSlideSheet.getValueAt(row,10).toString()!="N/A") { 
                        differential=Double.parseDouble(jTableSlideSheet.getValueAt(row,11).toString())-Double.parseDouble(jTableSlideSheet.getValueAt(row,10).toString());
                        jTableSlideSheet.setValueAt(na(differential),row,12);
                    } else
                        jTableSlideSheet.setValueAt("N/A",row,12);
                    jTableSlideSheet.setValueAt(na(oSlideSheetPerMd.getWob()),row,13); 
                    jTableSlideSheet.setValueAt(na(oSlideSheetPerMd.getSrpm()),row,14); 
                    jTableSlideSheet.setValueAt(na(oSlideSheetPerMd.getTorque()),row,15); 
                    jTableSlideSheet.setValueAt(na(oSlideSheetPerMd.getOffBotTorque()),row,16); 
                    //jTableSlideSheet.setValueAt(na(oSlideSheetPerMd.getDesiredPowerSetting()),row,17);
                    jTableSlideSheet.setValueAt(na(oSlideSheetPerMd.getPowerSetting()),row,17); //Son Equivalentes segun ultimos cxambios
                    row++;
                }
            }
            if (row>0) {
                jTableSlideSheet.setValueAt(na(md2),row-1,4) ;
                deltaMd=Double.parseDouble(jTableSlideSheet.getValueAt(row-1,4).toString())-Double.parseDouble(jTableSlideSheet.getValueAt(row-1,3).toString());
                jTableSlideSheet.setValueAt(na(deltaMd),row-1,5); 
            }
            for (int i=0;i<=row-1;i++) {
                sumatoriaDeltaMd+=Double.parseDouble(jTableSlideSheet.getValueAt(i,5).toString());
                if (jTableSlideSheet.getValueAt(i,17).toString()!="N/A")
                    sumatoriaDeltaMdPorDesiredPowerSetting+=Double.parseDouble(jTableSlideSheet.getValueAt(i,5).toString())*Double.parseDouble(jTableSlideSheet.getValueAt(i,17).toString());
                if ("Sliding".equals(jTableSlideSheet.getValueAt(i,6).toString().trim())) {
                    sumatoriaSliding+=Double.parseDouble(jTableSlideSheet.getValueAt(i,5).toString());
                }
            }
            porcentajeSliding=(sumatoriaSliding/sumatoriaDeltaMd)*100;
            porcentajeSteering=(sumatoriaDeltaMdPorDesiredPowerSetting/sumatoriaDeltaMd);
            if (porcentajeSliding>0)
                this.jLabelSlideSheet.setText("Slide Sheet  (% Sliding: "+na(porcentajeSliding)+")");
            if (porcentajeSteering>0)
                this.jLabelSlideSheet.setText("Slide Sheet  (% Steering: "+na(porcentajeSteering)+")");
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Rpt001.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    private void mostrarLAS() {
       LAS oLAS=new LAS();
       LASPerMD oLASPerMd=new LASPerMD();
       Object[] o;
       int p=this.jTableCorridas.getSelectedRow();
       long runId=aCorridas[p];
       long slideSheetId=0;
       Double md1=0.0,md2=0.0;
       Double md1_work=0.0,md2_work=0.0;
       int cantGrMayor130=0;
       int row=0; 
       double cantArena=0.0, porcentajeArena=0.0;
       long wellId=getWellId(runId);
       oCalc=new Calculos(oBD);
       oCalc.cargarPozo(wellId);       
       
       limpiarTablaLAS();
       this.jTableLAS.getColumnModel().getColumn(1).setCellRenderer(new MyRendererGr());
       this.jTableLAS.getColumnModel().getColumn(2).setCellRenderer(new MyRendererP40h());       
    
       md1=new Double(jTableSurvey.getValueAt(0, 0).toString());
       md2=new Double(jTableSurvey.getValueAt(1, 0).toString());   
       
       try {
           if (oBD.select(LAS.class, "runId="+runId).length>0){
                oLAS=(LAS) oBD.select(LAS.class, "runId="+runId)[0];
                slideSheetId=oLAS.getId();
                o=oBD.select(LASPerMD.class, "lasId="+slideSheetId);
                for (int i=0;i<=o.length-1;i++) {
                    oLASPerMd=(LASPerMD) o[i];
                    if (oLASPerMd.getDept()>=md1 && oLASPerMd.getDept()<=md2 ) {
                        modeloLAS.setRowCount(row+1);
                        jTableLAS.setValueAt(na(oLASPerMd.getDept()),row,0); 
                        jTableLAS.setValueAt(na(oLASPerMd.getGr()),row,1); 
                        cantGrMayor130+=(oLASPerMd.getGr()>130) ? 1:0;
                        jTableLAS.setValueAt(na(oLASPerMd.getP40hunc()),row,2); 
                        cantArena+=(oLASPerMd.getGr()<=45 && row>0 ) ? 0.5:0.0;
                        if (row==0) md1_work=oLASPerMd.getDept();
                        md2_work=oLASPerMd.getDept();
                        row++;
                    }
                }
                rowsInLas=row;               
           }
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Rpt001.class.getName()).log(Level.SEVERE, null, ex);
        }
        //porcentajeArena=(cantArena/(md2_work-md1_work))*100;
        porcentajeArena=oCalc.getPorcentajeArena(md2);
        this.jLabelPorcentajeArena.setText(("% Arena:"+na(porcentajeArena)));
        String type="";
        if (porcentajeArena>=80) {
            type="Sand";
        } else
        {
            if (porcentajeArena>=50) {
                type="Shale-Sand intercalation";
            } else
                type="Shale";
        }
        if (cantGrMayor130>4) type="Shale-Coal susppected";
        this.jLabelType.setText("("+type+")");
    }
    
    private long getWellId(long runId) {
        long wellId=0;
        ResultSet rs=null;
        String s="SELECT Sections.wellId FROM Sections INNER JOIN Run ON Sections.id = Run.sectionId ";
        s+="WHERE Run.Id="+runId;
        rs=oBD.select(s);
        try {
            if (rs.next()) {
                wellId=rs.getLong("wellId");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Rpt001.class.getName()).log(Level.SEVERE, null, ex);
        }
        return wellId;
    }
    
    private void graficar() {
        JFreeChart oJFreeChart1 = ChartFactory.createXYLineChart(
            "Gr" ,
            "Md (feet)" ,
            "gAPI" ,
            createDataset1(rowsInLas) ,
            PlotOrientation.HORIZONTAL ,
            false , true , false); 
               
        JFreeChart oJFreeChart2 = ChartFactory.createXYLineChart(
            "P40h x Md" ,
            "" ,
            "ohm" ,
            createDataset2(rowsInLas) ,
            PlotOrientation.HORIZONTAL ,
            false , true , false);
        
       
        XYPlot plot1 = (XYPlot) oJFreeChart1.getPlot();
        XYPlot plot2 = (XYPlot) oJFreeChart2.getPlot();

        plot1.getRenderer().setSeriesPaint(0, Color.green.darker());
        plot2.getRenderer().setSeriesPaint(0, Color.BLUE);
        
        oJFreeChart1.getXYPlot().getDomainAxis().setInverted(true);
        oJFreeChart2.getXYPlot().getDomainAxis().setInverted(true);
        
        ChartPanel Panel1 = new ChartPanel(oJFreeChart1);
        ChartPanel Panel2 = new ChartPanel(oJFreeChart2);
        
        Ventana = new JDialog(this,false);
        Ventana.setPreferredSize(new Dimension(600,680));
        Ventana.setMinimumSize(new Dimension(600,680));
        Ventana.setTitle("LAS");
        
        JPanel jp=new JPanel();
        jp.setLayout(new GridLayout());
        jp.add(Panel1);
        jp.add(Panel2);

        Ventana.add(jp);

        Ventana.pack();
        Ventana.setLocationRelativeTo(this);
        Ventana.setModal(true);
        Ventana.setVisible(true);
        Ventana.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private XYDataset createDataset1(int rows )
    {
       final XYSeries serieGr = new XYSeries( "Gr" );
        for (int i=0;i<=rows-1;i++) {
             try {
                 serieGr.add( Double.parseDouble((String) this.jTableLAS.getValueAt(i, 0)) , Double.parseDouble((String) this.jTableLAS.getValueAt(i, 1)) );
             }catch (NumberFormatException ex) {}
         }

       final XYSeriesCollection dataset = new XYSeriesCollection( );  
       dataset.addSeries( serieGr );     
       return dataset;
    }
    
    private XYDataset createDataset2(int rows )
    {
       final XYSeries serieRes = new XYSeries( "P40h" );
        for (int i=0;i<=rows-1;i++) {
             try {
                 serieRes.add( Double.parseDouble((String) this.jTableLAS.getValueAt(i, 0)) , Double.parseDouble((String) this.jTableLAS.getValueAt(i, 2)) );
             }catch (NumberFormatException ex) {}
         }
         

       final XYSeriesCollection dataset = new XYSeriesCollection( );  
       dataset.addSeries( serieRes );      
       return dataset;
    }
    
    private void prepararControlesDeCriteria() {
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
        limpiarTablaCorridas();
    }
    
    private void filtrarCorridas(double tvdDesde, double tvdHasta) {
        double tvdWork=0.0;
        Object[][] aTablaCorridas=new Object[this.jTableCorridas.getRowCount()][this.jTableCorridas.getColumnCount()];
        List<Long> listCorridasFiltradas = new ArrayList<>();
        Long[] aCorridasBck=aCorridas;
        int p=0;
        for (int i=0;i<=this.jTableCorridas.getRowCount()-1;i++) {
            for (int j=0;j<=this.jTableCorridas.getColumnCount()-1;j++) {
                aTablaCorridas[i][j]=this.jTableCorridas.getValueAt(i, j);                
            }
        }
        limpiarTablaCorridas();
        for (int i=0;i<=aTablaCorridas.length-1;i++){
            tvdWork=Double.parseDouble(aTablaCorridas[i][6].toString());
            if (tvdWork>=tvdDesde && tvdWork<=tvdHasta) {                
                for (int j=0;j<=aTablaCorridas[i].length-1;j++){
                    this.jTableCorridas.setValueAt(aTablaCorridas[i][j], p, j);                    
                }
                listCorridasFiltradas.add(aCorridasBck[i]);
                p++;                
            }
        }
        modeloCorridas.setRowCount(p);
        Object[] aCorridasWrk=listCorridasFiltradas.toArray();
        aCorridas=new Long[aCorridasWrk.length];
        for (int i=0;i<=aCorridasWrk.length-1;i++){
            aCorridas[i]=Long.parseLong(aCorridasWrk[i].toString());
        }
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

    private void calcularDistancia(){
       String s="",origen="";
       ResultSet rs;
       double lat1=0.0, long1=0.0;
       double lat2=0.0, long2=0.0;
       double d=0.0;
       int p=this.jTableCorridas.getSelectedRow();
       long runId=aCorridas[p];
       if (this.jTextFieldLongitud.getText().isEmpty()==false)
           long2=Double.parseDouble(this.jTextFieldLongitud.getText());
       if (this.jTextFieldLatitud.getText().isEmpty()==false)
           lat2=Double.parseDouble(this.jTextFieldLatitud.getText());
       s="SELECT Run.Id,Well.nombre, Well.locationLat, Well.locationLong\n" +
            "FROM Run INNER JOIN Well ON Run.wellId = Well.id\n" +
            "WHERE (((Run.Id)="+runId+"));";
       rs=oBD.select(s);
        try {
            if (rs.next()){
                lat1=rs.getDouble("locationLat");
                long1=rs.getDouble("locationLong");
                origen="Origen field: "+rs.getString("nombre")+", Lat:"+na(lat1)+", Long:"+na(long1);
            }} catch (SQLException ex) {
            Logger.getLogger(Rpt001.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (lat1>0 && lat2>0 && long1>0 && long2>0){
            d=Math.sqrt(Math.pow((long2-long1),2)+Math.pow((lat2-lat1),2));
        }
        this.jLabelOrigen.setText(origen);
        this.jLabelDistancia.setText("Distancia: "+na(d));
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
        buttonGroup2 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jRadioButtonCliente = new javax.swing.JRadioButton();
        jRadioButtonCampo = new javax.swing.JRadioButton();
        jRadioButtonMacolla = new javax.swing.JRadioButton();
        jComboBoxClientes = new javax.swing.JComboBox();
        jComboBoxCampo = new javax.swing.JComboBox();
        jComboBoxMacolla = new javax.swing.JComboBox();
        jComboBoxDrillingSubType = new javax.swing.JComboBox();
        jTextFieldDlsDesde = new javax.swing.JTextField();
        jTextFieldDlsHasta = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButtonBuscarCorridas = new javax.swing.JButton();
        jRadioButtonDls = new javax.swing.JRadioButton();
        jRadioButtonBr = new javax.swing.JRadioButton();
        jRadioButtonTr = new javax.swing.JRadioButton();
        jComboBoxInclinacion = new javax.swing.JComboBox();
        jScrollPaneCorridas = new javax.swing.JScrollPane();
        jTableCorridas = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jScrollPanePlan = new javax.swing.JScrollPane();
        jTablePlan = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jScrollPaneSurvey = new javax.swing.JScrollPane();
        jTableSurvey = new javax.swing.JTable();
        jLabelSlideSheet = new javax.swing.JLabel();
        jScrollPaneSlideSheet = new javax.swing.JScrollPane();
        jTableSlideSheet = new javax.swing.JTable();
        jScrollPaneLas = new javax.swing.JScrollPane();
        jTableLAS = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jLabelGrafica = new javax.swing.JLabel();
        jLabelPorcentajeArena = new javax.swing.JLabel();
        jLabelType = new javax.swing.JLabel();
        jLabelMostrarBHA = new javax.swing.JLabel();
        jTextFieldTVDHasta = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldTVDDesde = new javax.swing.JTextField();
        jButtonFiltrar = new javax.swing.JButton();
        jToggleButtonDistance = new javax.swing.JToggleButton();
        jTextFieldLongitud = new javax.swing.JTextField();
        jTextFieldLatitud = new javax.swing.JTextField();
        jLabelLatitud = new javax.swing.JLabel();
        jButtonCalcular = new javax.swing.JButton();
        jLabelLongitud = new javax.swing.JLabel();
        jLabelDistancia = new javax.swing.JLabel();
        jLabelOrigen = new javax.swing.JLabel();

        setTitle("Well Excecution Data");
        setMinimumSize(new java.awt.Dimension(1200, 600));
        setModal(true);
        setResizable(false);
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

        jComboBoxDrillingSubType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxDrillingSubTypeActionPerformed(evt);
            }
        });
        jPanel1.add(jComboBoxDrillingSubType, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 10, 190, -1));

        jTextFieldDlsDesde.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldDlsDesde.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldDlsDesdeActionPerformed(evt);
            }
        });
        jTextFieldDlsDesde.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldDlsDesdeKeyTyped(evt);
            }
        });
        jPanel1.add(jTextFieldDlsDesde, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 60, 50, -1));

        jTextFieldDlsHasta.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldDlsHasta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldDlsHastaActionPerformed(evt);
            }
        });
        jTextFieldDlsHasta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldDlsHastaKeyTyped(evt);
            }
        });
        jPanel1.add(jTextFieldDlsHasta, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 60, 50, -1));

        jLabel2.setText("Hasta:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 40, -1, -1));

        jLabel3.setText("Desde:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 40, -1, -1));

        jButtonBuscarCorridas.setText("Buscar Corridas");
        jButtonBuscarCorridas.setEnabled(false);
        jButtonBuscarCorridas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBuscarCorridasActionPerformed(evt);
            }
        });
        jPanel1.add(jButtonBuscarCorridas, new org.netbeans.lib.awtextra.AbsoluteConstraints(820, 60, -1, -1));

        buttonGroup2.add(jRadioButtonDls);
        jRadioButtonDls.setSelected(true);
        jRadioButtonDls.setText("dls");
        jRadioButtonDls.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonDlsActionPerformed(evt);
            }
        });
        jPanel1.add(jRadioButtonDls, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 10, -1, -1));

        buttonGroup2.add(jRadioButtonBr);
        jRadioButtonBr.setText("Br");
        jRadioButtonBr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonBrActionPerformed(evt);
            }
        });
        jPanel1.add(jRadioButtonBr, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 10, -1, -1));

        buttonGroup2.add(jRadioButtonTr);
        jRadioButtonTr.setText("Tr");
        jPanel1.add(jRadioButtonTr, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 10, -1, -1));

        jComboBoxInclinacion.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "inclinacion( 0 -29)", "inclinacion (30 - 59)", "inclinacion (60 - 74)", "inclinacion (75 100)" }));
        jComboBoxInclinacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxInclinacionActionPerformed(evt);
            }
        });
        jPanel1.add(jComboBoxInclinacion, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 10, 150, -1));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 10, 940, 100));

        jTableCorridas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Campo", "Macolla", "Pozo", "Seccion", "Corrida", "md", "tvd"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableCorridas.setEnabled(false);
        jTableCorridas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableCorridasMouseClicked(evt);
            }
        });
        jScrollPaneCorridas.setViewportView(jTableCorridas);

        getContentPane().add(jScrollPaneCorridas, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, 510, 140));

        jLabel4.setText("Plan");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 120, -1, -1));

        jTablePlan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "md", "incl", "azim", "tvd", "vsec", "ns", "ew", "dls", "tr", "br"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTablePlan.setEnabled(false);
        jScrollPanePlan.setViewportView(jTablePlan);

        getContentPane().add(jScrollPanePlan, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 140, 650, 50));

        jLabel5.setText("Survey");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 200, -1, -1));

        jTableSurvey.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "md", "incl", "azim", "tvd", "vsec", "ns", "ew", "dls", "tr", "br"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableSurvey.setEnabled(false);
        jScrollPaneSurvey.setViewportView(jTableSurvey);

        getContentPane().add(jScrollPaneSurvey, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 220, 650, 70));

        jLabelSlideSheet.setText("Slide Sheet");
        getContentPane().add(jLabelSlideSheet, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 310, -1, -1));

        jTableSlideSheet.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "fecha", "start time", "end time", "md from", "md to", "delta md", "operation mode", "tf mode", "tf angle", "flow", "spf off bott", "spf on bott", "differential", "wob", "srpm", "torque", "offBottTorque", "desired Power Setting"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableSlideSheet.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableSlideSheet.setEnabled(false);
        jScrollPaneSlideSheet.setViewportView(jTableSlideSheet);

        getContentPane().add(jScrollPaneSlideSheet, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 330, 840, 140));

        jTableLAS.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "dept", "gr", "p40hunc"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableLAS.setEnabled(false);
        jScrollPaneLas.setViewportView(jTableLAS);

        getContentPane().add(jScrollPaneLas, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 330, 310, 220));

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/my/SB.png"))); // NOI18N
        jLabel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel9MouseClicked(evt);
            }
        });
        getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 510, 140, 40));
        getContentPane().add(jSplitPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 420, -1, -1));

        jLabelGrafica.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelGrafica.setText("graficar");
        jLabelGrafica.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabelGrafica.setEnabled(false);
        jLabelGrafica.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jLabelGraficaFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jLabelGraficaFocusLost(evt);
            }
        });
        jLabelGrafica.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelGraficaMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabelGraficaMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelGraficaMouseExited(evt);
            }
        });
        getContentPane().add(jLabelGrafica, new org.netbeans.lib.awtextra.AbsoluteConstraints(1130, 300, 50, 20));

        jLabelPorcentajeArena.setText("% Arena:");
        getContentPane().add(jLabelPorcentajeArena, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 310, -1, -1));

        jLabelType.setText("          ");
        getContentPane().add(jLabelType, new org.netbeans.lib.awtextra.AbsoluteConstraints(970, 310, 150, -1));

        jLabelMostrarBHA.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelMostrarBHA.setText("Corridas (Mostrar BHA)");
        jLabelMostrarBHA.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabelMostrarBHA.setEnabled(false);
        jLabelMostrarBHA.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelMostrarBHAMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabelMostrarBHAMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelMostrarBHAMouseExited(evt);
            }
        });
        getContentPane().add(jLabelMostrarBHA, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 120, 150, -1));

        jTextFieldTVDHasta.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldTVDHasta.setEnabled(false);
        jTextFieldTVDHasta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTVDHastaActionPerformed(evt);
            }
        });
        jTextFieldTVDHasta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldTVDHastaKeyTyped(evt);
            }
        });
        getContentPane().add(jTextFieldTVDHasta, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 120, 60, -1));

        jLabel6.setText("Rango TVD:");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 120, -1, -1));

        jTextFieldTVDDesde.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldTVDDesde.setEnabled(false);
        jTextFieldTVDDesde.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTVDDesdeActionPerformed(evt);
            }
        });
        jTextFieldTVDDesde.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldTVDDesdeKeyTyped(evt);
            }
        });
        getContentPane().add(jTextFieldTVDDesde, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 120, 60, -1));

        jButtonFiltrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/my/filter.png"))); // NOI18N
        jButtonFiltrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonFiltrar.setEnabled(false);
        jButtonFiltrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFiltrarActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonFiltrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 120, 20, 20));

        jToggleButtonDistance.setIcon(new javax.swing.ImageIcon(getClass().getResource("/my/distance.png"))); // NOI18N
        jToggleButtonDistance.setEnabled(false);
        jToggleButtonDistance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonDistanceActionPerformed(evt);
            }
        });
        getContentPane().add(jToggleButtonDistance, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 40, 40));

        jTextFieldLongitud.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldLongitud.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldLongitudActionPerformed(evt);
            }
        });
        jTextFieldLongitud.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldLongitudKeyTyped(evt);
            }
        });
        getContentPane().add(jTextFieldLongitud, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 530, 90, -1));

        jTextFieldLatitud.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldLatitud.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldLatitudActionPerformed(evt);
            }
        });
        jTextFieldLatitud.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldLatitudKeyTyped(evt);
            }
        });
        getContentPane().add(jTextFieldLatitud, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 530, 90, -1));

        jLabelLatitud.setText("Latitud:");
        getContentPane().add(jLabelLatitud, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 530, -1, -1));

        jButtonCalcular.setText("Calc");
        jButtonCalcular.setEnabled(false);
        jButtonCalcular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCalcularActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonCalcular, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 530, 60, -1));

        jLabelLongitud.setText("Longitud:");
        getContentPane().add(jLabelLongitud, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 530, -1, -1));

        jLabelDistancia.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelDistancia.setText("Distancia:");
        getContentPane().add(jLabelDistancia, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 530, 190, -1));

        jLabelOrigen.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        jLabelOrigen.setText("Origen:");
        getContentPane().add(jLabelOrigen, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 490, -1, -1));

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
        limpiarTablaCorridas();
        clienteId=oManejoDeCombos.getComboID(this.jComboBoxClientes);
        String s="SELECT campoId,campoNombre from ConsultaCampoCliente1 WHERE clienteId="+clienteId;
        oManejoDeCombos.llenaCombo(oBD,oManejoDeCombos.getModeloCombo(),s,this.jComboBoxCampo,"Seleccione Campo");
    }//GEN-LAST:event_jComboBoxClientesActionPerformed

    private void jComboBoxCampoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxCampoActionPerformed
        limpiarTablaCorridas();
        clienteId=oManejoDeCombos.getComboID(this.jComboBoxClientes);
        campoId=oManejoDeCombos.getComboID(this.jComboBoxCampo);
        String s="SELECT macollaId,macollaNombre from ConsultaMacolla1 WHERE clienteId="+clienteId + " AND campoId="+campoId;
        oManejoDeCombos.llenaCombo(oBD,oManejoDeCombos.getModeloCombo(),s,this.jComboBoxMacolla,"Seleccione Macolla");
    }//GEN-LAST:event_jComboBoxCampoActionPerformed

    private void jComboBoxMacollaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxMacollaActionPerformed
        limpiarTablaCorridas();
        macollaId=oManejoDeCombos.getComboID(this.jComboBoxMacolla);
    }//GEN-LAST:event_jComboBoxMacollaActionPerformed

    private void jComboBoxDrillingSubTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxDrillingSubTypeActionPerformed
        sectionSubTypeId=oManejoDeCombos.getComboID(this.jComboBoxDrillingSubType);
    }//GEN-LAST:event_jComboBoxDrillingSubTypeActionPerformed

    private void jTextFieldDlsDesdeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldDlsDesdeActionPerformed

    }//GEN-LAST:event_jTextFieldDlsDesdeActionPerformed

    private void jTextFieldDlsDesdeKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldDlsDesdeKeyTyped
        if (jRadioButtonDls.isSelected())
            oNumeros.soloDobles(evt);
        else
            oNumeros.soloDoblesConNegativos(evt);
    }//GEN-LAST:event_jTextFieldDlsDesdeKeyTyped

    private void jTextFieldDlsHastaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldDlsHastaActionPerformed

    }//GEN-LAST:event_jTextFieldDlsHastaActionPerformed

    private void jTextFieldDlsHastaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldDlsHastaKeyTyped
        oNumeros.soloDoblesConNegativos(evt);
    }//GEN-LAST:event_jTextFieldDlsHastaKeyTyped

    private void jButtonBuscarCorridasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBuscarCorridasActionPerformed
        buscarCorridas();
    }//GEN-LAST:event_jButtonBuscarCorridasActionPerformed

    private void jTableCorridasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableCorridasMouseClicked
       if (jTableCorridas.isEnabled()){
            mostrarInfo();
            calcularDistancia();
       }
    }//GEN-LAST:event_jTableCorridasMouseClicked

    private void jLabel9MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MouseClicked
        msgbox(MainForm.msgVersion,"About WellDataReport");
    }//GEN-LAST:event_jLabel9MouseClicked

    private void jLabelGraficaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jLabelGraficaFocusGained

    }//GEN-LAST:event_jLabelGraficaFocusGained

    private void jLabelGraficaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jLabelGraficaFocusLost

    }//GEN-LAST:event_jLabelGraficaFocusLost

    private void jLabelGraficaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelGraficaMouseClicked
        graficar();
    }//GEN-LAST:event_jLabelGraficaMouseClicked

    private void jLabelGraficaMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelGraficaMouseEntered
        jLabelGrafica.setForeground(Color.BLUE);
    }//GEN-LAST:event_jLabelGraficaMouseEntered

    private void jLabelGraficaMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelGraficaMouseExited
        jLabelGrafica.setForeground(Color.BLACK);
    }//GEN-LAST:event_jLabelGraficaMouseExited

    private void jComboBoxInclinacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxInclinacionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxInclinacionActionPerformed

    private void jLabelMostrarBHAMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelMostrarBHAMouseEntered
        jLabelMostrarBHA.setForeground(Color.BLUE);
    }//GEN-LAST:event_jLabelMostrarBHAMouseEntered

    private void jLabelMostrarBHAMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelMostrarBHAMouseExited
        jLabelMostrarBHA.setForeground(Color.BLACK);
    }//GEN-LAST:event_jLabelMostrarBHAMouseExited

    private void jLabelMostrarBHAMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelMostrarBHAMouseClicked
        mostrarBHA();
    }//GEN-LAST:event_jLabelMostrarBHAMouseClicked

    private void jRadioButtonBrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonBrActionPerformed

    }//GEN-LAST:event_jRadioButtonBrActionPerformed

    private void jRadioButtonDlsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonDlsActionPerformed
        if (this.factorDesde<0) this.jTextFieldDlsDesde.setText("");
        if (this.factorHasta<0) this.jTextFieldDlsHasta.setText("");
    }//GEN-LAST:event_jRadioButtonDlsActionPerformed

    private void jTextFieldTVDHastaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTVDHastaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldTVDHastaActionPerformed

    private void jTextFieldTVDHastaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldTVDHastaKeyTyped
        oNumeros.soloDobles(evt);
    }//GEN-LAST:event_jTextFieldTVDHastaKeyTyped

    private void jTextFieldTVDDesdeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTVDDesdeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldTVDDesdeActionPerformed

    private void jTextFieldTVDDesdeKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldTVDDesdeKeyTyped
        oNumeros.soloDobles(evt);
    }//GEN-LAST:event_jTextFieldTVDDesdeKeyTyped

    private void jButtonFiltrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFiltrarActionPerformed
        filtrarCorridas(Double.parseDouble(this.jTextFieldTVDDesde.getText()),Double.parseDouble(this.jTextFieldTVDHasta.getText()));
    }//GEN-LAST:event_jButtonFiltrarActionPerformed

    private void jTextFieldLongitudActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldLongitudActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldLongitudActionPerformed

    private void jTextFieldLongitudKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldLongitudKeyTyped
        oNumeros.soloDobles(evt);
    }//GEN-LAST:event_jTextFieldLongitudKeyTyped

    private void jTextFieldLatitudActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldLatitudActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldLatitudActionPerformed

    private void jTextFieldLatitudKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldLatitudKeyTyped
        oNumeros.soloDobles(evt);
    }//GEN-LAST:event_jTextFieldLatitudKeyTyped

    private void jButtonCalcularActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCalcularActionPerformed
        calcularDistancia();
    }//GEN-LAST:event_jButtonCalcularActionPerformed

    private void jToggleButtonDistanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonDistanceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButtonDistanceActionPerformed

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
            java.util.logging.Logger.getLogger(Rpt001.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Rpt001.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Rpt001.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Rpt001.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Rpt001 dialog = new Rpt001(new javax.swing.JFrame(), true, new ManejoBDAccess());
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
    private javax.swing.ButtonGroup buttonGroup2;
    public javax.swing.JButton jButtonBuscarCorridas;
    private javax.swing.JButton jButtonCalcular;
    private javax.swing.JButton jButtonFiltrar;
    private javax.swing.JComboBox jComboBoxCampo;
    private javax.swing.JComboBox jComboBoxClientes;
    private javax.swing.JComboBox jComboBoxDrillingSubType;
    private javax.swing.JComboBox jComboBoxInclinacion;
    private javax.swing.JComboBox jComboBoxMacolla;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelDistancia;
    private javax.swing.JLabel jLabelGrafica;
    private javax.swing.JLabel jLabelLatitud;
    private javax.swing.JLabel jLabelLongitud;
    private javax.swing.JLabel jLabelMostrarBHA;
    private javax.swing.JLabel jLabelOrigen;
    private javax.swing.JLabel jLabelPorcentajeArena;
    private javax.swing.JLabel jLabelSlideSheet;
    private javax.swing.JLabel jLabelType;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButtonBr;
    private javax.swing.JRadioButton jRadioButtonCampo;
    private javax.swing.JRadioButton jRadioButtonCliente;
    private javax.swing.JRadioButton jRadioButtonDls;
    private javax.swing.JRadioButton jRadioButtonMacolla;
    private javax.swing.JRadioButton jRadioButtonTr;
    private javax.swing.JScrollPane jScrollPaneCorridas;
    private javax.swing.JScrollPane jScrollPaneLas;
    private javax.swing.JScrollPane jScrollPanePlan;
    private javax.swing.JScrollPane jScrollPaneSlideSheet;
    private javax.swing.JScrollPane jScrollPaneSurvey;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTableCorridas;
    private javax.swing.JTable jTableLAS;
    private javax.swing.JTable jTablePlan;
    private javax.swing.JTable jTableSlideSheet;
    private javax.swing.JTable jTableSurvey;
    private javax.swing.JTextField jTextFieldDlsDesde;
    private javax.swing.JTextField jTextFieldDlsHasta;
    private javax.swing.JTextField jTextFieldLatitud;
    private javax.swing.JTextField jTextFieldLongitud;
    private javax.swing.JTextField jTextFieldTVDDesde;
    private javax.swing.JTextField jTextFieldTVDHasta;
    private javax.swing.JToggleButton jToggleButtonDistance;
    // End of variables declaration//GEN-END:variables
}
 
