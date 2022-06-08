datadir = 'D:/MyProgram/java/MeteoInfoDev/toolbox/verification/sample'
fn = os.path.join(datadir, 'ex1.csv')
table = readtable(fn, delimiter=',', format='%3i%5f%i')
obs = table['Obs']
fcst = table['GFSMean']
scatter(obs, fcst, fill=False)
xlabel('Observation')
ylabel('Forecast')
ylim(-4, 18)
x = [-4, 18]
y = [-4, 18]
plot(x, y)
title('Most Basic')