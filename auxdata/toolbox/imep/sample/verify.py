datadir = 'D:/MyProgram/java/MeteoInfoDev/toolbox/verification/sample'
fn = os.path.join(datadir, 'ex1.csv')
table = readtable(fn, delimiter=',', format='%3i%5f%i')
obs = table['Obs']
fcst = table['GFSMean']
print 'Continuous verification...'
cmethod = verify.verifymethod(method='continuous')
ctable = verify.verifytable(obs, fcst, cmethod)
print ctable
print 'Dichotomous verification...'
dr = verify.datarange(min=8)    # >= 8
dmethod = verify.verifymethod(method='dichotomous', drange=dr)
dtable = verify.verifytable(obs, fcst, dmethod)
print dtable