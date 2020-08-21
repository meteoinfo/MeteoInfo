/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.laboratory.gui;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.action.CAction;
import bibliothek.gui.dock.common.action.CButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bibliothek.gui.dock.common.mode.ExtendedMode;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.meteoinfo.chart.ChartPanel;
import org.meteoinfo.chart.IChartPanel;
import org.meteoinfo.chart.MouseMode;
import org.meteoinfo.chart.jogl.GLChartPanel;
import org.meteoinfo.ui.ButtonTabComponent;
import org.scilab.forge.jlatexmath.Char;

/**
 *
 * @author wyq
 */
public class FigureDockable extends DefaultSingleCDockable {

    private final JTabbedPane tabbedPanel;
    private FrmMain parent;
    private boolean doubleBuffer;

    public FigureDockable(final FrmMain parent, String id, String title, CAction... actions) {
        super(id, title, actions);

        this.parent = parent;
        this.doubleBuffer = true;
        this.setTitleIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/figure.svg"));
        tabbedPanel = new JTabbedPane();
        tabbedPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                PythonInteractiveInterpreter interp = parent.getConsoleDockable().getInterpreter();
                if (tabbedPanel.getTabCount() == 0) {
                    try {
                        interp.exec("mipylib.plotlib.miplot.g_figure = None");
                        interp.exec("mipylib.plotlib.miplot.gca = None");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    interp.set("cp", getCurrentFigure());
                    interp.exec("mipylib.plotlib.miplot.g_figure = cp");
                    interp.exec("mipylib.plotlib.miplot.gca = None");
                }
            }
        });
        this.getContentPane().add(tabbedPanel);
        //this.setCloseable(false);

        //Add actions     
        //Select action
        CButton button = new CButton();
        button.setText("Select");
        //button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Arrow.png")));
        button.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/select.svg"));
        button.setTooltip("Select");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IChartPanel cp = FigureDockable.this.getCurrentFigure();
                if (cp != null) {
                    cp.setMouseMode(MouseMode.SELECT);
                }
            }
        });
        this.addAction(button);
        this.addSeparator();
        //Zoom in action
        button = new CButton();
        button.setText("Zoom In");
        //button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_ZoomIn.Image.png")));
        button.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/zoom-in.svg"));
        button.setTooltip("Zoom In");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IChartPanel cp = FigureDockable.this.getCurrentFigure();
                if (cp != null) {
                    cp.setMouseMode(MouseMode.ZOOM_IN);
                }
            }
        });
        this.addAction(button);
        //Zoom out action
        button = new CButton();
        button.setText("Zoom Out");
        //button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_ZoomOut.Image.png")));
        button.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/zoom-out.svg"));
        button.setTooltip("Zoom Out");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IChartPanel cp = FigureDockable.this.getCurrentFigure();
                if (cp != null) {
                    cp.setMouseMode(MouseMode.ZOOM_OUT);
                }
            }
        });
        this.addAction(button);
        //Pan action
        button = new CButton();
        button.setText("Pan");
        //button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_Pan.Image.png")));
        button.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/hand.svg"));
        button.setTooltip("Pan");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IChartPanel cp = FigureDockable.this.getCurrentFigure();
                if (cp != null) {
                    cp.setMouseMode(MouseMode.PAN);
                }
            }
        });
        this.addAction(button);
        //Rotate action
        button = new CButton();
        button.setText("Rotate");
        //button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rotate_16.png")));
        button.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/rotate_16.svg"));
        button.setTooltip("Rotate");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IChartPanel cp = FigureDockable.this.getCurrentFigure();
                if (cp != null) {
                    cp.setMouseMode(MouseMode.ROTATE);
                }
            }
        });
        this.addAction(button);
        //Full extent action
        button = new CButton();
        button.setText("Full Extent");
        //button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/TSB_FullExtent.Image.png")));
        button.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/full-extent.svg"));
        button.setTooltip("Full Extent");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IChartPanel cp = FigureDockable.this.getCurrentFigure();
                if (cp != null) {
                    cp.onUndoZoomClick();
                }
            }
        });
        this.addAction(button);
        this.addSeparator();
        //Identifer action
        button = new CButton();
        button.setText("Identifer");
        //button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/information.png")));
        button.setIcon(new FlatSVGIcon("org/meteoinfo/laboratory/icons/information.svg"));
        button.setTooltip("Identifer");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IChartPanel cp = FigureDockable.this.getCurrentFigure();
                if (cp != null) {
                    cp.setMouseMode(MouseMode.IDENTIFER);
                }
            }
        });
        this.addAction(button);
        this.addSeparator();
    }

    /**
     * Add a new figure
     *
     * @param title Figure title
     * @param cp
     * @return Figure chart panel
     */
    public final IChartPanel addNewFigure(String title, final JPanel cp) {
        if (cp instanceof ChartPanel) {
            ((ChartPanel)cp).setDoubleBuffer(this.doubleBuffer);
        }
        final JScrollPane sp = new JScrollPane(cp);
        this.tabbedPanel.add(sp, title);
        this.tabbedPanel.setSelectedComponent(sp);
        ButtonTabComponent btc = new ButtonTabComponent(tabbedPanel);
        JButton button = btc.getTabButton();
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeFigure(cp);
            }
        });
        tabbedPanel.setTabComponentAt(tabbedPanel.indexOfComponent(sp), btc);

        return (IChartPanel) cp;
    }

    /**
     * Add a new figure
     *
     * @param ncp
     * @return Figure chart panel
     */
    public final IChartPanel addFigure(final JPanel ncp) {
        if (ncp instanceof ChartPanel) {
            ((ChartPanel) ncp).setDoubleBuffer(this.doubleBuffer);
        }
        int idx = 1;
        if (this.tabbedPanel.getTabCount() > 0) {
            List<Integer> idxes = new ArrayList<>();
            for (int i = 0; i < this.tabbedPanel.getTabCount(); i++) {
                String text = this.tabbedPanel.getTitleAt(i);
                String[] strs = text.split("\\s+");
                if (strs.length > 1) {
                    String idxStr = strs[strs.length - 1];
                    try {
                        idx = Integer.parseInt(idxStr);
                        idxes.add(idx);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
            Collections.sort(idxes);
            idx = 1;
            boolean isIn = false;
            for (int i : idxes) {
                if (idx < i) {
                    isIn = true;
                    break;
                }
                idx += 1;
            }

            if (!isIn) {
                idx = idxes.size() + 1;
            }
        }

        final JScrollPane sp = new JScrollPane(ncp);
        this.tabbedPanel.add(sp, "Figure " + String.valueOf(idx));
        this.tabbedPanel.setSelectedComponent(sp);
        final ButtonTabComponent btc = new ButtonTabComponent(tabbedPanel);
        JButton button = btc.getTabButton();
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeFigure(ncp);
            }
        });
        tabbedPanel.setTabComponentAt(tabbedPanel.indexOfComponent(sp), btc);

        return (IChartPanel) ncp;
    }

    /**
     * Remove chart panel
     * @param cp Chart panel
     */
    public void removeFigure(JPanel cp) {
        if (cp instanceof GLChartPanel) {
            ((GLChartPanel) cp).animator_stop();
        }
        JScrollPane sp = (JScrollPane) cp.getParent().getParent();
        if (tabbedPanel.getTabCount() > 0) {
            tabbedPanel.remove(sp);
        }
        PythonInteractiveInterpreter interp = parent.getConsoleDockable().getInterpreter();
        if (tabbedPanel.getTabCount() == 0) {
            try {
                interp.exec("mipylib.plotlib.miplot.chartpanel = None");
                interp.exec("mipylib.plotlib.miplot.c_plot = None");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            interp.set("cp", getCurrentFigure());
            interp.exec("mipylib.plotlib.miplot.chartpanel = cp");
            interp.exec("mipylib.plotlib.miplot.c_plot = None");
        }
    }

    /**
     * Get current figure
     *
     * @return Figure
     */
    public IChartPanel getCurrentFigure() {
        if (this.tabbedPanel.getTabCount() == 0) {
            return null;
        }
        JScrollPane sp = (JScrollPane) this.tabbedPanel.getSelectedComponent();
        return (IChartPanel) sp.getViewport().getView();
    }

    /**
     * Get figure
     *
     * @param idx Figure index
     * @return Figure
     */
    public ChartPanel getFigure(int idx) {
        if (this.tabbedPanel.getTabCount() > idx) {
            JScrollPane sp = (JScrollPane) this.tabbedPanel.getComponentAt(idx);
            return (ChartPanel) sp.getViewport().getView();
        } else {
            return null;
        }
    }

    /**
     * Get figure number
     * @return Figure number
     */
    public int getFigureNumber() {
        return this.tabbedPanel.getTabCount();
    }

    /**
     * Get figures
     * @return Figures
     */
    public List<ChartPanel> getFigures() {
        List<ChartPanel> figures = new ArrayList<>();
        for (int i = 0; i < this.tabbedPanel.getTabCount(); i++) {
            figures.add(this.getFigure(i));
        }
        return figures;
    }

    /**
     * Set current figure
     *
     * @param cp ChartPanel
     */
    public void setCurrentFigure(ChartPanel cp) {
        if (this.tabbedPanel.getTabCount() > 0) {
            JScrollPane sp = new JScrollPane(cp);
            this.tabbedPanel.setComponentAt(this.tabbedPanel.getSelectedIndex(), sp);
        }
    }

    /**
     * Set double buffering
     * @param doubleBuffer Double buffering or not
     */
    public void setDoubleBuffer(boolean doubleBuffer) {
        this.doubleBuffer = doubleBuffer;
        List<ChartPanel> figures = this.getFigures();
        for (ChartPanel figure : figures) {
            figure.setDoubleBuffer(this.doubleBuffer);
            figure.repaintNew();
        }
    }
}
