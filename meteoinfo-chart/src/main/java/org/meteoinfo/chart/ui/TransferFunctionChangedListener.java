package org.meteoinfo.chart.ui;

import java.util.EventListener;

public interface TransferFunctionChangedListener extends EventListener {
    public void transferFunctionChangedEvent(TransferFunctionChangedEvent e);
}
