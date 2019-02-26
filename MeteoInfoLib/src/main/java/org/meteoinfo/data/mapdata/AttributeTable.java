/* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.data.mapdata;

import org.meteoinfo.table.DataColumn;
import org.meteoinfo.table.DataRow;
import org.meteoinfo.table.DataTable;
import org.meteoinfo.data.DataTypes;
import org.meteoinfo.io.EndianDataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Attribute table of shape file
 *
 * @author yaqiang
 */
public final class AttributeTable implements Cloneable {
    // <editor-fold desc="Variables">

    private int _numRecords;
    private Date _updateDate;
    private int _headerLength;
    private int _recordLength;
    private int _numFields;
    private List<Field> _columns;
    private byte _fileType;
    private EndianDataOutputStream _writer;
    private File _file;
    //private String _fileName;
    private DataTable _dataTable;
    // Constant for the size of a record
    private final int FileDescriptorSize = 32;
    private boolean _attributesPopulated;
    private char[] _characterContent;
    private byte[] _byteContent;
    private long[] _offsets;
    private boolean _hasDeletedRecords;
    //private Stopwatch _dataRowWatch;
    private boolean _loaded;
    //private bool _virtualMode;
    private List<Integer> _deletedRows;
    private String encoding = "UTF-8";
    // </editor-fold>

    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public AttributeTable() {
        configure();
    }

    /**
     * Constructor
     *
     * @param filename The file name
     * @throws java.io.FileNotFoundException
     */
    public AttributeTable(String filename) throws FileNotFoundException, IOException, Exception {
        _file = new File(filename);
        configure();
        File aFile = new File(filename);
        if (aFile.exists()) {
            open(filename);
        }
    }

    private void configure() {
        _fileType = 0x03;
        _dataTable = new DataTable();
        _columns = new ArrayList<>();
        _attributesPopulated = true; // only turn this false during an "open" method
        _deletedRows = new ArrayList<>();
        _characterContent = new char[1];
    }
    // </editor-fold>

    // <editor-fold desc="Get Set Methods">
    /**
     * Get file
     *
     * @return File
     */
    public File getFile() {
        return this._file;
    }

    /**
     * Get record number
     *
     * @return Record number
     */
    public int getNumRecords() {
        return _dataTable.getRows().size();
    }

    /**
     * Get data table
     *
     * @return The data table
     */
    public DataTable getTable() {
        return _dataTable;
    }

    /**
     * Set data table
     *
     * @param table The data table
     */
    public void setTable(DataTable table) {
        _dataTable = table;
    }

    /**
     * Get encoding string
     *
     * @return Encoding string
     */
    public String getEncoding() {
        return this.encoding;
    }

    /**
     * Set encoding string
     *
     * @param value Encoding string
     */
    public void setEncoding(String value) {
        this.encoding = value;
    }
    // </editor-fold>

    // <editor-fold desc="Methods">
    /**
     * Update data table - convert DataColumn to Field
     */
    public void updateDataTable() {
        for (DataColumn col : _dataTable.getColumns()) {
            if (col.getClass().equals(DataColumn.class)) {
                col = new Field(col);
            }
        }
    }

    /**
     * Reads all the information from the file, including the vector shapes and
     * the database component.
     *
     * @param filename The file name
     * @throws java.io.FileNotFoundException
     */
    public void open(String filename) throws FileNotFoundException, IOException, Exception {
        String fileName = filename.replace(filename.substring(filename.lastIndexOf(".")), ".dbf");
        File file = new File(fileName);
        if (!file.exists()) {
            fileName = filename.replace(filename.substring(filename.lastIndexOf(".")), ".DBF");
        }
        openDBF(fileName);
    }

    /**
     * Reads all the information from the dBase file, including the vector
     * shapes and the database component.
     *
     * @param fileName The dBase file name
     * @throws java.io.FileNotFoundException
     */
    public void openDBF(String fileName) throws FileNotFoundException, IOException, Exception {
        _attributesPopulated = false; // we had a file, but have not read the dbf content into memory yet.
        _dataTable = new DataTable();
        _file = new File(fileName);
        if (!_file.exists()) {
            System.out.println("The dbf file for this shapefile was not found.");
            return;
        }

        DataInputStream myReader = new DataInputStream(new BufferedInputStream(new FileInputStream(_file)));
        readTableHeader(myReader); // based on the header, set up the fields information etc.

        //_hasDeletedRecords = false;
//            FileInfo fi = new FileInfo(_fileName);
//            if (fi.Length == (_headerLength + 1) + _numRecords * _recordLength)
//            {
//                _hasDeletedRecords = false;
//                // No deleted rows detected
//                return;
//            }
//            _hasDeletedRecords = true;
//            int count = 0;
//            int row = 0;
//            while (count < _numRecords)
//            {
//                if (myStream.ReadByte() == (byte)' ')
//                {
//                    count++;
//                }
//                else
//                {
//                    _deletedRows.Add(row);
//                }
//                row++;
//                myStream.Seek(_recordLength - 1, SeekOrigin.Current);
//            }
        myReader.close();
    }

    /**
     * Read the header data from the DBF file.
     *
     * @param reader
     */
    private void readTableHeader(DataInputStream reader) throws IOException, Exception {
        byte[] arr = new byte[12];
        reader.read(arr);
        ByteBuffer buffer = ByteBuffer.wrap(arr);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        // type of reader.
        _fileType = buffer.get();
        if (_fileType != 0x03) {
            throw new Exception("Unsupported DBF reader Type " + _fileType);
        }

        // parse the update date information.
        int year = buffer.get();
        int month = buffer.get();
        int day = buffer.get();
        Calendar cal = Calendar.getInstance();
        cal.set(year + 1900, month - 1, day);
        _updateDate = cal.getTime();

        // read the number of records.
        _numRecords = buffer.getInt();

        // read the length of the header structure.
        _headerLength = buffer.getShort();

        // read the length of a record
        _recordLength = buffer.getShort();

        // skip the reserved bytes in the header.
        //in.skipBytes(20);
        reader.skipBytes(20);

        // calculate the number of Fields in the header
        _numFields = (_headerLength - FileDescriptorSize - 1) / FileDescriptorSize;

        // _numFields = (_headerLength - FileDescriptorSize) / FileDescriptorSize;
        _columns = new ArrayList<>();

        for (int i = 0; i < _numFields; i++) {
            arr = new byte[18];
            reader.read(arr);
            buffer = ByteBuffer.wrap(arr);

            // read the field name				            
            byte[] bytes = new byte[11];
            buffer.get(bytes);
            String name = new String(bytes, this.encoding);

            int nullPoint = name.indexOf((char) 0);
            if (nullPoint != -1) {
                name = name.substring(0, nullPoint);
            }

            // read the field type
            char Code = (char) buffer.get();

            // read the field data address, offset from the start of the record.
            int dataAddress = buffer.getInt();

            // read the field length in bytes
            int tempLength = buffer.get() & 0xFF;

            // read the field decimal count in bytes
            byte decimalcount = buffer.get();

            // read the reserved bytes.
            reader.skipBytes(14);
            int j = 1;
            String tempName = name;
            while (_dataTable.getColumnNames().contains(tempName)) {
                tempName = name + j;
                j++;
            }
            name = tempName;
            Field myField = new Field(name, Code, tempLength, decimalcount);
            //myField.DataAddress = dataAddress; // not sure what this does yet

            _columns.add(myField); // Store fields accessible by an index
            _dataTable.getColumns().add(myField);
        }

        // Last byte is a marker for the end of the field definitions.
        reader.readByte();
    }

    private void load() throws FileNotFoundException, IOException {
        RandomAccessFile rafo = new RandomAccessFile(_file, "r");
        //FileChannel fco = rafo.getChannel();
        //MappedByteBuffer myReader = fco.map(FileChannel.MapMode.READ_ONLY, 0, fco.size());

        //FileInfo fi = new FileInfo(_fileName);
        // Encoding appears to be ASCII, not Unicode
        rafo.seek(_headerLength + 1);
        //((Buffer) myReader).position(_headerLength + 1);
        if ((int) rafo.length() == _headerLength) {
            // The file is empty, so we are done here
            return;
        }
        int length = (int) rafo.length() - (_headerLength) - 1;
        _byteContent = new byte[length];
        //myReader.get(_byteContent);
        rafo.read(_byteContent);
        //fco.close();
        rafo.close();
        //_characterContent = new char[length];            
        //Encoding.Default.GetChars(_byteContent, 0, length, _characterContent, 0);
        if (_hasDeletedRecords) {
            int recordCount = length / _recordLength;
            _offsets = new long[_numRecords];
            int j = 0; // undeleted index
            for (int i = 0; i <= recordCount; i++) {
                //if (_characterContent[i * _recordLength] != '*')
                //    _offsets[j] = i * _recordLength;
                if ((char) (_byteContent[i * _recordLength]) != '*') {
                    _offsets[j] = i * _recordLength;
                }
                j++;
                if (j == _numRecords) {
                    break;
                }
            }
        }
        _loaded = true;
    }

    /**
     * This populates the Table with data from the file.
     *
     * @param numRows In the event that the dbf file is not found, this
     * indicates how many blank rows should exist in the attribute Table.
     * @throws java.io.FileNotFoundException
     */
    public void fill(int numRows) throws FileNotFoundException, IOException, Exception {
        if (!_loaded) {
            load();
        }
        //_dataRowWatch = new Stopwatch();

        _dataTable.getRows().clear(); // if we have already loaded data, clear the data.

        //File aFile = new File(_fileName);
        if (!_file.exists()) {
            _numRecords = numRows;
            //_dataTable.BeginLoadData();
            _dataTable.addColumn("FID", DataTypes.Integer);
            for (int row = 0; row < numRows; row++) {
                DataRow dr = _dataTable.newRow();
                dr.setValue("FID", row);
                _dataTable.addRow(dr);
            }
            //_dataTable.EndLoadData();
            return;
        }
        //Stopwatch sw = new Stopwatch();
        //sw.Start();

        //_dataTable.BeginLoadData();
        // Reading the Table elements as well as the shapes in a single progress loop.
        for (int row = 0; row < _numRecords; row++) {
            // --------- DATABASE --------- CurrentFeature = ReadTableRow(myReader);
            try {
                //_dataTable.Rows.Add(ReadTableRowFromChars(row));
                _dataTable.addRow(readTableRowFromBytes(row));
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                _dataTable.addRow(_dataTable.newRow());
            }
        }
        //_dataTable.EndLoadData();
        //sw.Stop();

        //Debug.WriteLine("Load Time:" + sw.ElapsedMilliseconds + " Milliseconds");
        //Debug.WriteLine("Conversion:" + _dataRowWatch.ElapsedMilliseconds + " Milliseconds");
        _attributesPopulated = true;
        //onAttributesFilled();
    }

    private DataRow readTableRowFromBytes(int currentRow) throws Exception {
        DataRow result = _dataTable.newRow();

        long start;
        if (_hasDeletedRecords == false) {
            start = currentRow * _recordLength;
        } else {
            start = _offsets[currentRow];
        }

        for (int col = 0; col < _dataTable.getColumns().size(); col++) {
            // find the length of the field.
            Field CurrentField = _columns.get(col);

            // find the field type
            char tempFieldType = CurrentField.getTypeCharacter();

            // read the data.
            //char[] cBuffer = new char[CurrentField.Length];
            byte[] cBuffer = Arrays.copyOfRange(_byteContent, (int) start, (int) start + CurrentField.getLength());
            //Array.copy(_byteContent, start, cBuffer, 0, CurrentField.getLength());
            //Array.Copy(_characterContent, start, bBuffer, 0, CurrentField.Length);
            start += CurrentField.getLength();

            Object tempObject = null;
            //if (IsNull(cBuffer)) continue;

            switch (tempFieldType) {
                case 'L': // logical data type, one character (T,t,F,f,Y,y,N,n)

                    char tempChar = (char) cBuffer[0];
                    if ((tempChar == 'T') || (tempChar == 't') || (tempChar == 'Y') || (tempChar == 'y')) {
                        tempObject = true;
                    } else {
                        tempObject = false;
                    }
                    break;
                case 'C': // character record.
                    tempObject = new String(cBuffer, this.encoding).trim();
                    break;
                case 'T':
                    throw new Exception();
                case 'D': // date data type.
                    byte[] dBuffer = Arrays.copyOfRange(cBuffer, 0, 4);
                    String tempString = new String(dBuffer);
                    int year = Integer.parseInt(tempString);
                    dBuffer = Arrays.copyOfRange(cBuffer, 4, 6);
                    tempString = new String(dBuffer);
                    int month = Integer.parseInt(tempString);
                    dBuffer = Arrays.copyOfRange(cBuffer, 6, 8);
                    tempString = new String(dBuffer);
                    int day = Integer.parseInt(tempString);
                    Calendar cal = new GregorianCalendar(year, month - 1, day);
                    tempObject = cal.getTime();
                    break;
                case 'F':
                case 'B':
                case 'N': // number - ESRI uses N for doubles and floats
                    String tempStr = new String(cBuffer);
                    if (!tempStr.trim().equals("null")) {
                        if (tempStr.trim().isEmpty()) {
                            tempStr = "0";
                        }
                        DataTypes t = CurrentField.getDataType();
                        switch (t) {
                            case Integer:
                                tempObject = Integer.parseInt(tempStr.trim());
                                break;
                            case Float:
                                tempObject = Float.parseFloat(tempStr);
                                break;
                            case Double:
                                tempObject = Double.parseDouble(tempStr);
                                break;
                        }
                    }
                    break;

                default:
                    throw new Exception("Do not know how to parse Field type " + tempFieldType);
            }

            result.setValue(CurrentField.getColumnName(), tempObject);
        }

        return result;
    }

    /**
     * Save the file
     */
    public void save() {
        try {
            //File theFile = new File(this._fileName);
            updateSchema();
            _writer = new EndianDataOutputStream(new BufferedOutputStream(new FileOutputStream(_file)));
            writeHeader(_writer);
            writeTable();
            _writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AttributeTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AttributeTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Save the file
     *
     * @param fileName File name
     */
    public void save(String fileName) {
        try {
            //File theFile = new File(this._fileName);
            updateSchema();
            _writer = new EndianDataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
            writeHeader(_writer);
            writeTable();
            _writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AttributeTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AttributeTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Save this table to the specified file name
     *
     * @param fileName The file name to be saved
     * @param overwrite If over write the file if it exists
     */
    public void saveAs(String fileName, boolean overwrite) {
        if (_file == null) {
            _file = new File(fileName);
            save();
        } else {
            if (_file.getAbsoluteFile().equals(fileName)) {
                save();
                return;
            }
            if (new File(fileName).exists()) {
                if (overwrite == false) {
                    if (JOptionPane.showConfirmDialog(null, "The file " + fileName + " already exists.  Do you wish to overwrite it?",
                            "File Exists", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
            }
            save(fileName);
        }
    }

    private void updateSchema() {
        List<Field> tempColumns = new ArrayList<>();
        _recordLength = 1; // delete character
        _numRecords = this._dataTable.getRows().size();
        _updateDate = new Date();
        _headerLength = FileDescriptorSize + FileDescriptorSize * _dataTable.getColumns().size() + 1;
        if (_columns == null) {
            _columns = new ArrayList<>();
        }
        // Delete any fields from the columns list that are no 
        // longer in the data Table.
        List<Field> removeFields = new ArrayList<>();
        for (Field fld : _columns) {
            if (!_dataTable.getColumnNames().contains(fld.getColumnName())) {
                removeFields.add(fld);
            } else {
                tempColumns.add(fld);
            }
        }
        for (Field field : removeFields) {
            _columns.remove(field);
        }

        // Add new columns that exist in the data Table, but don't have a matching field yet.
        if (_dataTable.getColumns() != null) {
            for (DataColumn dc : _dataTable.getColumns()) {
                if (columnNameExists(dc.getColumnName())) {
                    continue;
                }
//                Field fld = (Field) dc;
//                if (fld == null) {
//                    fld = new Field(dc);
//                }

                Field fld = new Field(dc);

                tempColumns.add(fld);
            }
        }

        _columns = tempColumns;

        // Recalculate the recordlength
        for (Field fld : _columns) {
            //_recordLength = _recordLength + fld.Length + 1;
            _recordLength = _recordLength + fld.getLength();
        }
    }

    private boolean columnNameExists(String name) {
        for (Field fld : _columns) {
            if (fld.getColumnName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Write the header data to the DBF file
     *
     * @param writer The writer stream
     * @throws IOException IO exception
     */
    public void writeHeader(EndianDataOutputStream writer) throws IOException {
        // write the output file type.
        writer.writeByteLE(_fileType);
        Calendar calendar = new GregorianCalendar();
        Date trialTime = new Date();
        calendar.setTime(trialTime);
        writer.writeByteLE(calendar.get(Calendar.YEAR) - 1900);
        writer.writeByteLE(calendar.get(Calendar.MONTH) + 1); // month is 0-indexed
        writer.writeByteLE(calendar.get(Calendar.DAY_OF_MONTH));

        // write the number of records in the datafile.
        writer.writeIntLE(_numRecords);

        // write the length of the header structure.
        writer.writeShortLE((short) _headerLength); // 32 + 30 * numColumns

        // write the length of a record
        writer.writeShortLE((short) _recordLength);

        // write the reserved bytes in the header
        for (int i = 0; i < 20; i++) {
            writer.writeByteLE((byte) 0);
        }

        // write all of the header records
        Field currentField;
        for (int i = 0; i < _columns.size(); i++) {
            currentField = _columns.get(i);
            // write the field name
            byte[] bytes = currentField.getColumnName().getBytes(this.encoding);
            for (int j = 0; j < 11; j++) {
                if (bytes.length > j) {
                    writer.writeByteLE(bytes[j]);
                } else {
                    writer.writeByteLE((byte) 0);
                }
            }

            // write the field type
            writer.writeByteLE(currentField.getTypeCharacter());

            // write the field data address, offset from the start of the record.
            writer.writeIntLE(0);

            // write the length of the field.
            writer.writeByteLE(currentField.getLength());

            // write the decimal count.
            writer.writeByteLE(currentField.getDecimalCount());

            // write the reserved bytes.
            for (int j = 0; j < 14; j++) {
                writer.writeByteLE((byte) 0);
            }
        }
        // write the end of the field definitions marker
        writer.writeByteLE((byte) 0x0D);
    }

    /**
     * This appends the content of one datarow to a DBF file
     *
     * @throws java.io.IOException
     */
    public void writeTable() throws IOException {
        if (_dataTable == null) {
            return;
        }

        for (int row = 0; row < _dataTable.getRows().size(); row++) {
            _writer.writeByteLE((byte) 0x20); // the deleted flag
            //int len = _recordLength - 1;
            StringBuffer tmps;
            //String s;
            for (int fld = 0; fld < _columns.size(); fld++) {
                Field currentField = _columns.get(fld);
                String name = currentField.getColumnName();

                Object columnValue = _dataTable.getRows().get(row).getValue(name);
                switch (currentField.getTypeCharacter()) {
                    case 'C':
                    case 'c':
                    case 'M':
                    case 'G':
                        //chars
                        String ss = (String) columnValue;
                        while (ss.length() < currentField.getLength()) {
                            //need to fill it with ' ' chars
                            //this should converge quickly
                            ss = ss + "                                                                                                                  ";
                        }
                        tmps = new StringBuffer(ss);
                        tmps.setLength(currentField.getLength());
                        //patch from Hisaji Ono for Double byte characters
                        _writer.write(tmps.toString().getBytes(this.encoding), 0, currentField.getLength());  // [Matthias Scholz 04.Sept.2010] Charset added
                        break;
                    case 'D':
                        //Date
                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                        String ds = format.format(columnValue);
                        _writer.write(ds.getBytes(), 0, ds.length());
                        break;
                    case 'N':
                    case 'n':
                        // int?
                        String fs;
                        if (currentField.getDataType() == DataTypes.Integer) {
                            fs = String.format("%1$" + String.valueOf(currentField.getLength()) + "d", columnValue);
                        } else {
                            fs = String.format("%1$" + String.valueOf(currentField.getLength()) + "."
                                    + String.valueOf(currentField.getDecimalCount()) + "f", columnValue);
                        }
                        if (fs.length() > currentField.getLength()) {
                            fs = fs.substring(0, currentField.getLength());
                        }
                        _writer.writeBytesLE(fs);
                        break;

//                        if (currentField.getDecimalCount() == 0) {
//                            if (columnValue instanceof Integer) {
//                                fs = FormatedString.format(((Integer) columnValue).intValue(), currentField.getLength());
//                            } // case LONG added by mmichaud on 18 sept. 2004
//                            else if (columnValue instanceof Long) {
//                                fs = FormatedString.format(((Long) columnValue).toString(), 0, currentField.getLength());
//                            } else if (columnValue instanceof java.math.BigDecimal) {
//                                fs = FormatedString.format(((BigDecimal) columnValue).toString(), 0, currentField.getLength());
//                            } else;
//                            if (fs.length() > currentField.getLength()) {
//                                fs = FormatedString.format(0, currentField.getLength());
//                            }
//                            _writer.writeBytesLE(fs);
//                            break;
//                        } else {
//                            if (columnValue instanceof Double) {
//                                fs = FormatedString.format(((Double) columnValue).toString(), currentField.getDecimalCount(), currentField.getLength());
//                            } else if (columnValue instanceof java.math.BigDecimal) {
//                                fs = FormatedString.format(((BigDecimal) columnValue).toString(), currentField.getDecimalCount(), currentField.getLength());
//                            } else;
//                            if (fs.length() > currentField.getLength()) {
//                                fs = FormatedString.format("0.0", currentField.getDecimalCount(), currentField.getLength());
//                            }
//                            _writer.writeBytesLE(fs);
//                            break;
//                        }
                    case 'F':
                    case 'f':
                        //double
                        //s = ((Double) columnValue).toString();
                        //String x = FormatedString.format(s, currentField.getDecimalCount(), currentField.getLength());
                        String x = String.format("%1$" + String.valueOf(currentField.getLength()) + "."
                                + String.valueOf(currentField.getDecimalCount()) + "f", columnValue);
                        _writer.writeBytesLE(x);
                        break;
                    // Case 'logical' added by mmichaud on 18 sept. 2004
                    case 'L':
                        //boolean
                        if (columnValue == null || columnValue.equals("") || columnValue.equals(" ")) {
                            _writer.writeBytesLE(" ");
                        } else {
                            boolean b = (Boolean) columnValue;
                            _writer.writeBytesLE(b ? "T" : "F");
                        }
                        break;
                }
            }
        }
    }

    /**
     * Clone
     *
     * @return AttributeTable object
     */
    @Override
    public Object clone() {
        AttributeTable newAT = new AttributeTable();
        newAT.setTable((DataTable) _dataTable.clone());

        return newAT;
    }
    // </editor-fold>
}
