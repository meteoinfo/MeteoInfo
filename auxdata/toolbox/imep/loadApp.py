from org.meteoinfo.ui.plugin import PluginBase, IApplication
from javax.swing import JFrame, JMenuItem
import inspect
import os
import sys
from org.meteothink.imep.forms import FrmMain

class LoadApp(PluginBase):
    
    def __init__(self):
        self.setName("IMEP")
        self.setAuthor("Yaqiang Wang")
        self.setVersion("0.8")
        self.setDescription("IMEP - Verification application")
        self.appMI = None
        
        this_file = inspect.getfile(inspect.currentframe())
        self.path = os.path.abspath(os.path.dirname(this_file))
        #print self.path

    def load(self):
        if self.appMI is None:
            self.appMI = JMenuItem('IMEP', None,\
                actionPerformed=self.onAppClick)
        app = self.getApplication()
        mainMenuBar = app.getMainMenuBar()
        appMenu = app.getPluginMenu()
        appMenu.add(self.appMI)
        mainMenuBar.validate()
        
    def unload(self):
        if not self.appMI is None:
            self.getApplication().getPluginMenu().remove(self.appMI)
            self.getApplication().getMainMenuBar().repaint()

    def onAppClick(self, e):
        frmMain = FrmMain()
        frmMain.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        #frmMain.size = (800, 500)
        frmMain.locationRelativeTo = None
        frmMain.visible = True
        
if __name__ == '__main__':
    app = LoadApp()
    app.onAppClick(None)