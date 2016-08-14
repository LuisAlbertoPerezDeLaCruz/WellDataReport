/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import miLibreria.bd.ManejoBDI;
import miLibreria.bd.*;

/**
 *
 * @author Luis
 */
public class Calculos {
    private ManejoBDI oBD;
    private Sections[] aSections;
    private Run[] aRun;
    private Survey[] aSurvey;
    private SurveyPerMD[] aSurveyPerMD;
    private SlideSheet[] aSlideSheet;
    private SlideSheetPerMD[] aSlideSheetPerMD;
    private LAS[] aLAS;
    private LASPerMD[] aLASPerMD;
    private long wellId;
    private final int valorNulo=-9898989;
    
    public Calculos (ManejoBDI o ) {
        oBD=o;
    }
    
    public double getPromedioTfAngleSliding(double prevMd, double currMd) {
        double promedioTfAngle=0.0;
        double ssMdFrom=0.0, ssMdTo=0.0;
        double tfAngle=0.0;
        long n=0;
        for (int i=0;i<=aSlideSheetPerMD.length-1;i++) {
           ssMdFrom=aSlideSheetPerMD[i].getMdFrom();
           ssMdTo=aSlideSheetPerMD[i].getMdTo();
           tfAngle=aSlideSheetPerMD[i].getTfAngle();
           if (tfAngle==valorNulo || tfAngle==0) continue;
           if (ssMdTo>=prevMd && ssMdFrom<=currMd) {
                if ("Sliding".equals(aSlideSheetPerMD[i].getDrillingMode()) || "Sliding".equals(aSlideSheetPerMD[i].getOperationMode())) {
                    promedioTfAngle+=tfAngle;
                    n++;
                }                
           }
        }
        if (n>0) promedioTfAngle/=n;
        return promedioTfAngle;
    }
    
    public double getPromedioSrpmRotating(double prevMd, double currMd) {
        double promedioSRPM=0.0;
        double ssMdFrom=0.0, ssMdTo=0.0;
        double srpm=0.0;
        long n=0;
        for (int i=0;i<=aSlideSheetPerMD.length-1;i++) {
           ssMdFrom=aSlideSheetPerMD[i].getMdFrom();
           ssMdTo=aSlideSheetPerMD[i].getMdTo();
           srpm=aSlideSheetPerMD[i].getSrpm();
           if (ssMdTo>=prevMd && ssMdFrom<=currMd) {
                if ("Rotating".equals(aSlideSheetPerMD[i].getDrillingMode()) || "Rotating".equals(aSlideSheetPerMD[i].getOperationMode())) {
                    promedioSRPM+=srpm;
                    n++;
                }                
           }
        }
        if (n>0) promedioSRPM/=n;
        return promedioSRPM;
    }
    
    public double getPromedioDifferential(double prevMd, double currMd) {
        double promedioDifferential=0.0;
        double ssMdFrom=0.0, ssMdTo=0.0;
        double differential=0.0;
        long n=0;
        for (int i=0;i<=aSlideSheetPerMD.length-1;i++) {
           ssMdFrom=aSlideSheetPerMD[i].getMdFrom();
           ssMdTo=aSlideSheetPerMD[i].getMdTo();
           differential=aSlideSheetPerMD[i].getDifferential();
           if (ssMdTo>=prevMd && ssMdFrom<=currMd) {
                promedioDifferential+=differential;
                n++;
           }
        }
        if (n>0) promedioDifferential/=n;
        return promedioDifferential;
    }
    
    public double getPromedioFlow(double prevMd, double currMd) {
        double promedioFlow=0.0;
        double ssMdFrom=0.0, ssMdTo=0.0;
        double flow=0.0;
        long n=0;
        for (int i=0;i<=aSlideSheetPerMD.length-1;i++) {
           ssMdFrom=aSlideSheetPerMD[i].getMdFrom();
           ssMdTo=aSlideSheetPerMD[i].getMdTo();
           flow=aSlideSheetPerMD[i].getFlow();
           if (ssMdTo>=prevMd && ssMdFrom<=currMd) {
                promedioFlow+=flow;
                n++;
           }
        }
        if (n>0) promedioFlow/=n;
        return promedioFlow;
    }
    
    public double getPromedioWob(double prevMd, double currMd) {
        double promedioWob=0.0;
        double ssMdFrom=0.0, ssMdTo=0.0;
        double wob=0.0;
        long n=0;
        for (int i=0;i<=aSlideSheetPerMD.length-1;i++) {
           ssMdFrom=aSlideSheetPerMD[i].getMdFrom();
           ssMdTo=aSlideSheetPerMD[i].getMdTo();
           wob=aSlideSheetPerMD[i].getWob();
           if (ssMdTo>=prevMd && ssMdFrom<=currMd) {
                promedioWob+=wob;
                n++;
           }
        }
        if (n>0) promedioWob/=n;
        return promedioWob;
    }

  
    public double getPorcentajeSliding(double prevMd, double currMd) {
        double porcentajeSliding=0.0;
        double deltaMd=0.0, sumatoriaDeltaMd=0.0, sumatoriaSliding=0.0;
        double prevSumatoriaDeltaMd=0.0, prevSumatoriaSliding=0.0;
        double ssMdFrom=0.0, ssMdTo=0.0;
        int r=0;
        int p=0,p1=0;
        for (int i=0;i<=aSlideSheetPerMD.length-1;i++) {
           ssMdFrom=aSlideSheetPerMD[i].getMdFrom();
           ssMdTo=aSlideSheetPerMD[i].getMdTo();
           if (ssMdTo>=prevMd && ssMdFrom<=currMd) {
               if (r==0) {
                   deltaMd=ssMdTo-prevMd;
               } else {
                   deltaMd=ssMdTo-ssMdFrom;
               }
               prevSumatoriaDeltaMd=sumatoriaDeltaMd;
               sumatoriaDeltaMd+=deltaMd;
               if ("Sliding".equals(aSlideSheetPerMD[i].getDrillingMode()) || "Sliding".equals(aSlideSheetPerMD[i].getOperationMode())) {
                   prevSumatoriaSliding=sumatoriaSliding;
                   sumatoriaSliding+=deltaMd;
                   p1=i;
               }
               p=i;
               r++;
           }
        }
        if (r>0) {
            sumatoriaDeltaMd=prevSumatoriaDeltaMd;
            deltaMd=currMd-aSlideSheetPerMD[p].getMdFrom();
            sumatoriaDeltaMd+=deltaMd;
            if (p1==p && p>0) {
               sumatoriaSliding=prevSumatoriaSliding;
               sumatoriaSliding+=deltaMd;
            }            
        }
        if (sumatoriaDeltaMd>0) {
            porcentajeSliding=(sumatoriaSliding/sumatoriaDeltaMd)*100;
        }        
        return porcentajeSliding;
    }
    
    public double getPorcentajeSliding(double currMd) {
        return getPorcentajeSliding(mdAnterior(currMd), currMd);
    }
    
    public double getPromedioWob(double currMd) {
        return getPromedioWob(mdAnterior(currMd), currMd);
    }
    
    public double getPromedioFlow(double currMd) {
        return getPromedioFlow(mdAnterior(currMd), currMd);
    }
    
    public double getPromedioSrpmRotating(double currMd) {
        return getPromedioSrpmRotating(mdAnterior(currMd), currMd);
    }
    
    public double getPromedioDifferential(double currMd) {
        return getPromedioDifferential(mdAnterior(currMd), currMd);
    }
    
    public double getPromedioTfAngleSliding(double currMd) {
        return getPromedioTfAngleSliding(mdAnterior(currMd), currMd);
    }
    
    public double getDeltaMdSliding(double prevMd, double currMd) {
        double deltaMd=0.0, sumatoriaDeltaMd=0.0, sumatoriaSliding=0.0;
        double prevSumatoriaDeltaMd=0.0, prevSumatoriaSliding=0.0;
        double ssMdFrom=0.0, ssMdTo=0.0;
        int r=0;
        int p=0,p1=0;
        for (int i=0;i<=aSlideSheetPerMD.length-1;i++) {
           ssMdFrom=aSlideSheetPerMD[i].getMdFrom();
           ssMdTo=aSlideSheetPerMD[i].getMdTo();
           if (ssMdTo>=prevMd && ssMdFrom<=currMd) {
               if (r==0) {
                   deltaMd=ssMdTo-prevMd;
               } else {
                   deltaMd=ssMdTo-ssMdFrom;
               }
               prevSumatoriaDeltaMd=sumatoriaDeltaMd;
               sumatoriaDeltaMd+=deltaMd;
               if ("Sliding".equals(aSlideSheetPerMD[i].getDrillingMode()) || "Sliding".equals(aSlideSheetPerMD[i].getOperationMode())) {
                   prevSumatoriaSliding=sumatoriaSliding;
                   sumatoriaSliding+=deltaMd;
                   p1=i;
               }
               p=i;
               r++;
           }
        }
        if (r>0) {
            sumatoriaDeltaMd=prevSumatoriaDeltaMd;
            deltaMd=currMd-aSlideSheetPerMD[p].getMdFrom();
            sumatoriaDeltaMd+=deltaMd;
            if (p1==p && p>0) {
               sumatoriaSliding=prevSumatoriaSliding;
               sumatoriaSliding+=deltaMd;
            }            
        }
        
        return sumatoriaSliding;
    } 
    
    public double getDeltaMdSliding(double currMd) {
        return getDeltaMdSliding(mdAnterior(currMd), currMd);        
    }
    
    public double getPorcentajeSteering(double prevMd, double currMd) {
        double porcentajeSteering=0.0;
        double deltaMd=0.0, sumatoriaDeltaMd=0.0, sumatoriaSteering=0.0;
        double prevSumatoriaDeltaMd=0.0, prevSumatoriaSteering=0.0;
        double ssMdFrom=0.0, ssMdTo=0.0;
        double desiredPowerSetting=0.0;
        double prevDesiredPowerSetting=0.0;
        int r=0;
        int p=0,p1=0;
        for (int i=0;i<=aSlideSheetPerMD.length-1;i++) {
           ssMdFrom=aSlideSheetPerMD[i].getMdFrom();
           ssMdTo=aSlideSheetPerMD[i].getMdTo();
           //desiredPowerSetting=aSlideSheetPerMD[i].getDesiredPowerSetting();
           desiredPowerSetting=aSlideSheetPerMD[i].getPowerSetting(); //Exiete equivalencia (ultimo cambio)
           if (ssMdTo>=prevMd && ssMdFrom<=currMd) {
               if (r==0) {
                   deltaMd=ssMdTo-prevMd;
               } else {
                   deltaMd=ssMdTo-ssMdFrom;
               } 
               prevSumatoriaDeltaMd=sumatoriaDeltaMd;
               sumatoriaDeltaMd+=deltaMd;
               if (desiredPowerSetting>0) {
                   prevSumatoriaSteering=sumatoriaSteering;
                   prevDesiredPowerSetting=desiredPowerSetting;
                   sumatoriaSteering+=deltaMd*desiredPowerSetting;
                   p1=i;
               }               
               r++;
               p=i;
           }
        }
        if (r>0) {
            sumatoriaDeltaMd=prevSumatoriaDeltaMd;
            deltaMd=currMd-aSlideSheetPerMD[p].getMdFrom();
            sumatoriaDeltaMd+=deltaMd;
            if (p1==p && p>0) {
               sumatoriaSteering=prevSumatoriaSteering;
               sumatoriaSteering+=deltaMd*prevDesiredPowerSetting;
            }  
            porcentajeSteering=sumatoriaSteering/sumatoriaDeltaMd;            
        }
        return porcentajeSteering;
    }
    
    public double getPorcentajeSteering(double currMd) {
        return getPorcentajeSteering(mdAnterior(currMd), currMd);        
    }
    
    public double getDeltaMdSteering(double prevMd, double currMd) {
        double deltaMd=0.0, sumatoriaDeltaMd=0.0, sumatoriaSteering=0.0;
        double prevSumatoriaDeltaMd=0.0, prevSumatoriaSteering=0.0;
        double ssMdFrom=0.0, ssMdTo=0.0;
        double desiredPowerSetting=0.0;
        double prevDesiredPowerSetting=0.0;
        int r=0;
        int p=0,p1=0;
        for (int i=0;i<=aSlideSheetPerMD.length-1;i++) {
           ssMdFrom=aSlideSheetPerMD[i].getMdFrom();
           ssMdTo=aSlideSheetPerMD[i].getMdTo();
           //desiredPowerSetting=aSlideSheetPerMD[i].getDesiredPowerSetting();
           desiredPowerSetting=aSlideSheetPerMD[i].getPowerSetting(); // Son equilaventes segun ultimos cambios
           if (ssMdTo>=prevMd && ssMdFrom<=currMd) {
               if (r==0) {
                   deltaMd=ssMdTo-prevMd;
               } else {
                   deltaMd=ssMdTo-ssMdFrom;
               } 
               prevSumatoriaDeltaMd=sumatoriaDeltaMd;
               sumatoriaDeltaMd+=deltaMd;
               if (desiredPowerSetting>0) {
                   prevSumatoriaSteering=sumatoriaSteering;
                   prevDesiredPowerSetting=desiredPowerSetting;
                   sumatoriaSteering+=deltaMd*desiredPowerSetting;
                   p1=i;
               }               
               r++;
               p=i;
           }
        }
        if (r>0) {
            sumatoriaDeltaMd=prevSumatoriaDeltaMd;
            deltaMd=currMd-aSlideSheetPerMD[p].getMdFrom();
            sumatoriaDeltaMd+=deltaMd;
            if (p1==p && p>0) {
               sumatoriaSteering=prevSumatoriaSteering;
               sumatoriaSteering+=deltaMd*prevDesiredPowerSetting;
            }            
        }
        return sumatoriaSteering;
    }
    
    public double getDeltaMdSteering(double currMd) {
        return getDeltaMdSteering(mdAnterior(currMd), currMd);        
    }
    
    public double getDlsOverDeltaMdSliding(double currMd) {
        double var=getDeltaMdSliding(currMd);
        if (var>0)
            return getDls(currMd)/var;
        else return 0;
    }
    
    public double getDlsOverDeltaMdSteering(double currMd) {
        double var=getDeltaMdSteering(currMd);
        if (var>0)
            return (getDls(currMd)/var)*100;
        else return 0;
    }    
    
    public void cargarPozo(long wellId_) {
        wellId=wellId_;
        @SuppressWarnings("UnusedAssignment")
        Sections oSections=null;
        Run oRun=null;
        Survey oSurvey=null;
        SurveyPerMD oSurveyPerMD=null;
        SlideSheet oSlideSheet=null;
        SlideSheetPerMD oSlideSheetPerMD=null;
        LAS oLAS=null;
        LASPerMD oLASPerMD=null;
        String s="";
        
        try {
            Object[] o=oBD.select(Sections.class, "wellId="+wellId);
            aSections=new Sections[o.length];
            for (int i=0;i<=o.length-1;i++) {
                oSections=new Sections();
                oSections=(Sections) o[i];
                aSections[i]=oSections;
            }
            
            s="";
            for (int i=0;i<=aSections.length-1;i++) {
                s+="sectionId="+aSections[i].getId();
                s+=(i<aSections.length-1) ? " OR " : "";
            }            
            o=oBD.select(Run.class, s);
            aRun=new Run[o.length];
            for (int i=0;i<=o.length-1;i++) {
                oRun=new Run();
                oRun=(Run) o[i];
                aRun[i]=oRun;
            }
            
            s="";
            for (int i=0;i<=aRun.length-1;i++) {
                s+="runId="+aRun[i].getId();
                s+=(i<aRun.length-1) ? " OR " : "";
            }            
            o=oBD.select(Survey.class, s);
            aSurvey=new Survey[o.length];
            for (int i=0;i<=o.length-1;i++) {
                oSurvey=new Survey();
                oSurvey=(Survey) o[i];
                aSurvey[i]=oSurvey;
            }
            
            s="";
            for (int i=0;i<=aSurvey.length-1;i++) {
                s+="surveyId="+aSurvey[i].getId();
                s+=(i<aSurvey.length-1) ? " OR " : "";
            }            
            o=oBD.select(SurveyPerMD.class, s + " ORDER BY md");            
            aSurveyPerMD=new SurveyPerMD[o.length];
            for (int i=0;i<=o.length-1;i++) {
                oSurveyPerMD=new SurveyPerMD();
                oSurveyPerMD=(SurveyPerMD) o[i];
                aSurveyPerMD[i]=oSurveyPerMD;
            } 
            
            s="";
            for (int i=0;i<=aRun.length-1;i++) {
                s+="runId="+aRun[i].getId();
                s+=(i<aRun.length-1) ? " OR " : "";
            }            
            o=oBD.select(SlideSheet.class, s);
            aSlideSheet=new SlideSheet[o.length];
            for (int i=0;i<=o.length-1;i++) {
                oSlideSheet=new SlideSheet();
                oSlideSheet=(SlideSheet) o[i];
                aSlideSheet[i]=oSlideSheet;
            }
            
            s="";
            for (int i=0;i<=aSlideSheet.length-1;i++) {
                s+="slideSheetId="+aSlideSheet[i].getId();
                s+=(i<aSlideSheet.length-1) ? " OR " : "";
            }            
            o=oBD.select(SlideSheetPerMD.class, s + " ORDER BY mdFrom");            
            aSlideSheetPerMD=new SlideSheetPerMD[o.length];
            for (int i=0;i<=o.length-1;i++) {
                oSlideSheetPerMD=new SlideSheetPerMD();
                oSlideSheetPerMD=(SlideSheetPerMD) o[i];
                aSlideSheetPerMD[i]=oSlideSheetPerMD;
            }
            
            s="";
            for (int i=0;i<=aRun.length-1;i++) {
                s+="runId="+aRun[i].getId();
                s+=(i<aRun.length-1) ? " OR " : "";
            }            
            o=oBD.select(LAS.class, s);
            aLAS=new LAS[o.length];
            for (int i=0;i<=o.length-1;i++) {
                oLAS=new LAS();
                oLAS=(LAS) o[i];
                aLAS[i]=oLAS;
            }
            
            s="";
            for (int i=0;i<=aLAS.length-1;i++) {
                s+="lasId="+aLAS[i].getId();
                s+=(i<aLAS.length-1) ? " OR " : "";
            }            
            o=oBD.select(LASPerMD.class, s + " ORDER BY dept");            
            aLASPerMD=new LASPerMD[o.length];
            for (int i=0;i<=o.length-1;i++) {
                oLASPerMD=new LASPerMD();
                oLASPerMD=(LASPerMD) o[i];
                aLASPerMD[i]=oLASPerMD;
            } 
            
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Calculos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void cargarSecciones(long wellId_) {
        wellId=wellId_;
        Sections oSections=null;
        try {
            Object[] o=oBD.select(Sections.class, "wellId="+wellId);
            aSections=new Sections[o.length];
            for (int i=0;i<=o.length-1;i++) {
                oSections=new Sections();
                oSections=(Sections) o[i];
                aSections[i]=oSections;
            }
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Calculos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void cargarPozo(long wellId_, long sectionId) {
        wellId=wellId_;
        Sections oSections=null;
        Run oRun=null;
        Survey oSurvey=null;
        SurveyPerMD oSurveyPerMD=null;
        SlideSheet oSlideSheet=null;
        SlideSheetPerMD oSlideSheetPerMD=null;
        LAS oLAS=null;
        LASPerMD oLASPerMD=null;
        String s="";
        
        try {
            Object[] o=oBD.select(Sections.class, "wellId="+wellId + " AND id="+sectionId);
            aSections=new Sections[o.length];
            for (int i=0;i<=o.length-1;i++) {
                oSections=new Sections();
                oSections=(Sections) o[i];
                aSections[i]=oSections;
            }
            
            s="";
            for (int i=0;i<=aSections.length-1;i++) {
                s+="sectionId="+aSections[i].getId();
                s+=(i<aSections.length-1) ? " OR " : "";
            }            
            o=oBD.select(Run.class, s);
            aRun=new Run[o.length];
            for (int i=0;i<=o.length-1;i++) {
                oRun=new Run();
                oRun=(Run) o[i];
                aRun[i]=oRun;
            }
            
            s="";
            for (int i=0;i<=aRun.length-1;i++) {
                s+="runId="+aRun[i].getId();
                s+=(i<aRun.length-1) ? " OR " : "";
            }            
            o=oBD.select(Survey.class, s);
            aSurvey=new Survey[o.length];
            for (int i=0;i<=o.length-1;i++) {
                oSurvey=new Survey();
                oSurvey=(Survey) o[i];
                aSurvey[i]=oSurvey;
            }
            
            s="";
            for (int i=0;i<=aSurvey.length-1;i++) {
                s+="surveyId="+aSurvey[i].getId();
                s+=(i<aSurvey.length-1) ? " OR " : "";
            }            
            o=oBD.select(SurveyPerMD.class, s + " ORDER BY md");            
            aSurveyPerMD=new SurveyPerMD[o.length];
            for (int i=0;i<=o.length-1;i++) {
                oSurveyPerMD=new SurveyPerMD();
                oSurveyPerMD=(SurveyPerMD) o[i];
                aSurveyPerMD[i]=oSurveyPerMD;
            } 
            
            s="";
            for (int i=0;i<=aRun.length-1;i++) {
                s+="runId="+aRun[i].getId();
                s+=(i<aRun.length-1) ? " OR " : "";
            }            
            o=oBD.select(SlideSheet.class, s);
            aSlideSheet=new SlideSheet[o.length];
            for (int i=0;i<=o.length-1;i++) {
                oSlideSheet=new SlideSheet();
                oSlideSheet=(SlideSheet) o[i];
                aSlideSheet[i]=oSlideSheet;
            }
            
            s="";
            for (int i=0;i<=aSlideSheet.length-1;i++) {
                s+="slideSheetId="+aSlideSheet[i].getId();
                s+=(i<aSlideSheet.length-1) ? " OR " : "";
            }            
            o=oBD.select(SlideSheetPerMD.class, s + " ORDER BY mdFrom");            
            aSlideSheetPerMD=new SlideSheetPerMD[o.length];
            for (int i=0;i<=o.length-1;i++) {
                oSlideSheetPerMD=new SlideSheetPerMD();
                oSlideSheetPerMD=(SlideSheetPerMD) o[i];
                aSlideSheetPerMD[i]=oSlideSheetPerMD;
            }
            
            s="";
            for (int i=0;i<=aRun.length-1;i++) {
                s+="runId="+aRun[i].getId();
                s+=(i<aRun.length-1) ? " OR " : "";
            }            
            o=oBD.select(LAS.class, s);
            aLAS=new LAS[o.length];
            for (int i=0;i<=o.length-1;i++) {
                oLAS=new LAS();
                oLAS=(LAS) o[i];
                aLAS[i]=oLAS;
            }
            
            s="";
            for (int i=0;i<=aLAS.length-1;i++) {
                s+="lasId="+aLAS[i].getId();
                s+=(i<aLAS.length-1) ? " OR " : "";
            }            
            o=oBD.select(LASPerMD.class, s + " ORDER BY dept");            
            aLASPerMD=new LASPerMD[o.length];
            for (int i=0;i<=o.length-1;i++) {
                oLASPerMD=new LASPerMD();
                oLASPerMD=(LASPerMD) o[i];
                aLASPerMD[i]=oLASPerMD;
            } 
            
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Calculos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean mdIguales(double md1, double md2) {
        boolean ok=false;
        if (Math.abs(md2-md1)<0.1) ok=true;
        return ok;
    } 
    
    public double getTVD(double md) {
        double tvd=0.0;
        for (int i=0;i<=aSurveyPerMD.length-1;i++) {
            if (mdIguales(md,aSurveyPerMD[i].getMd())) {
               tvd=aSurveyPerMD[i].getTvd();
               break;
            }
        }        
        return tvd;
    }
    
    public double getDls(double md) {
        double dls=0.0;
        for (int i=0;i<=aSurveyPerMD.length-1;i++) {
            if (mdIguales(md,aSurveyPerMD[i].getMd())) {
               dls=aSurveyPerMD[i].getDls();
               break;
            }
        }        
        return dls;
    }
    
    public double getGR(double md) {
        double gr=0.0;
        double dept=dameElDeptParaBuscarElGr(md);
        for (int i=0;i<=aLASPerMD.length-1;i++) {
            if (dept==aLASPerMD[i].getDept()) {
               gr=aLASPerMD[i].getGr();
               break;
            }
        }        
        return gr;
    }
    
    public double getPorcentajeArena_(double md) {
        double porcentajeArena=0.0;
        double currMd=md;
        double prevMd=mdAnterior(md);
        double dept=0.0;
        double cantArena=0.0;
        int r=0;
        double md1_work=0.0, md2_work=0.0;
        double cantTotalPies=0.0;
        
        for (int i=0;i<=aLASPerMD.length-1;i++) {
           dept=aLASPerMD[i].getDept();
           if (dept>=prevMd && dept<=currMd) {
              if (r==0) md1_work=dept;
              if (aLASPerMD[i].getGr()<=60) cantArena+=0.5; 
              cantTotalPies+=0.5;
              md2_work=dept;
              r++;
           }
        }
        //if (md2_work-md1_work >0)
        //    porcentajeArena=(cantArena/(md2_work-md1_work))*100;
            
        if (cantTotalPies>0)
            porcentajeArena=cantArena/cantTotalPies*100;
        return porcentajeArena;
    }
    
    public double getPorcentajeArena(double md) {
        double porcentajeArena=0.0;
        double currMd=md;
        double prevMd=mdAnterior(md);
        double dept=0.0;
        double cantArena=0.0;
        double cantTotalPies=0.0;
        
        for (int i=0;i<=aLASPerMD.length-1;i++) {
           dept=aLASPerMD[i].getDept();
           if (dept>=prevMd && dept<=currMd) {
              if (aLASPerMD[i].getGr()<=60) cantArena+=0.5; 
              cantTotalPies+=0.5;
           }
        }
            
        if (cantTotalPies>0)
            porcentajeArena=cantArena/cantTotalPies*100;
        return porcentajeArena;
    }    
    
    public double getCantGrMayor130(double md) {
        double cantGrMayor130=0;
        double currMd=md;
        double prevMd=mdAnterior(md);
        double dept=0.0;
        for (int i=0;i<=aLASPerMD.length-1;i++) {
           dept=aLASPerMD[i].getDept();
           if (dept>=prevMd && dept<=currMd) {
              if (aLASPerMD[i].getGr()>130) cantGrMayor130+=1.0; 
           }
        }
        return cantGrMayor130;
    }
    
    public String getClasificacion(double md) {
        double porcentajeArena=getPorcentajeArena(md);
        double cantGrMayor130=getCantGrMayor130(md);
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
        return type;
    }
    
    public String getDrillingSubSectionType(double md_) {
        String drillingSubSectionType="";
        String s="";
        ResultSet rs=null;
        long runId=0, drillingSubSectionId=0;
        Long md=Math.round(md_);
        
        s="SELECT Id FROM Run where wellId="+wellId+ " AND initialDepth<="+md + " AND ";
        s+="finalDepth>="+md+";";
        rs=oBD.select(s);
        try {
            if (rs.next()) {
                runId=rs.getLong("Id");
                s="SELECT drillingSubSectionId FROM RunSubSection ";
                s+="WHERE runId="+runId;
                s+=" AND profundidadInicial<="+md+" AND profundidadFinal>="+md+";";
                rs=oBD.select(s);
                if (rs.next()) {
                   drillingSubSectionId=rs.getLong("drillingSubSectionId");
                   s="SELECT description FROM drillingSubSectionType ";
                   s+="WHERE Id="+drillingSubSectionId+";";
                   rs=oBD.select(s);
                   if (rs.next()) {
                      drillingSubSectionType=rs.getString("description");
                   }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Calculos.class.getName()).log(Level.SEVERE, null, ex);
        }
        return drillingSubSectionType;
    }
    
    public Sections[] getSections() {
        return aSections;
    }
    
    public SurveyPerMD[] getSurveyPerMd() {
        return aSurveyPerMD;
    }
    
    public double mdAnterior(double md) {
        double mdAnterior=0.0;
        for (int i=0;i<=aSurveyPerMD.length-1;i++) {
            if (mdIguales(md,aSurveyPerMD[i].getMd())) {
               if (i>0) mdAnterior=aSurveyPerMD[i-1].getMd();
               if (i==0) mdAnterior=aSurveyPerMD[i].getMd();
               break;
            }
        }
        return mdAnterior;        
    }
    
    public String getWellNombre() {
        Well oWell=new Well();
        String wellNombre="";
        try {
            oWell=(Well) oBD.select(Well.class, "Id="+wellId)[0];
            wellNombre=oWell.getNombre();
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Rpt003.class.getName()).log(Level.SEVERE, null, ex);
        }
        return wellNombre;
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
    
}
