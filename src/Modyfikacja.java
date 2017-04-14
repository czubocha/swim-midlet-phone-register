import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;

public class Modyfikacja extends Form implements CommandListener {

    Command zapisz;
    Image img;
    ImageItem imgItem;
    TextField tfmodel;
    Gauge gram;
    ChoiceGroup cgsim;
    DateField dfdataZakupu;
    int _id;
    Displayable _parametry;
    String _data;

    public Modyfikacja(int id, Displayable parametry)
	    throws IOException, RecordStoreFullException, RecordStoreNotFoundException, RecordStoreException {
	super("Modyfikacja");
	_id = id;
	_parametry = parametry;
	zapisz = new Command("Zapisz", Command.OK, 0);
	addCommand(MojMidlet2.powrot);
	addCommand(zapisz);
	setCommandListener(this);

	append("Wprowadz dane telefonu:");
	// img = Image.createImage("/nexus.jpg");
	// append(new ImageItem(null, img, ImageItem.LAYOUT_DEFAULT, null));

	byte[] recData = new byte[100];
	ByteArrayInputStream strmBytes = new ByteArrayInputStream(recData);
	DataInputStream strmDataType = new DataInputStream(strmBytes);

	MojMidlet2.records.getRecord(id, recData, 0);
	String model = strmDataType.readUTF();
	int ram = strmDataType.readInt();
	int sim = strmDataType.readInt();
	_data = strmDataType.readUTF();

	tfmodel = new TextField("Model:", model, 256, TextField.ANY);
	append(tfmodel);
	gram = new Gauge("RAM:", true, 9, ram);
	append(gram);
	String[] simNames = { "Mini-SIM", "Micro-SIM", "Nano-SIM" };
	cgsim = new ChoiceGroup("Typ karty SIM:", ChoiceGroup.EXCLUSIVE, simNames, null);
	append(cgsim);
	cgsim.setSelectedIndex(sim, true);
	append("Data zakupu: " + _data);
//	dfdataZakupu = new DateField("Data zakupu:", DateField.DATE);
//	append(dfdataZakupu);
//	int dzien = Integer.parseInt(_data.substring(0, _data.indexOf(".")));
//	int miesiac = Integer.parseInt(_data.substring(_data.indexOf(".", 2), _data.indexOf(".", 3)));
//	int rok = Integer.parseInt(_data.substring(_data.indexOf(".", 4), _data.length()));
//	System.out.println(dzien);
//	System.out.println(miesiac);
//	System.out.println(rok);
//	Date date = new Date(0);
//	Calendar cal = Calendar.getInstance();
//	cal.set(Calendar.YEAR,);
//	cal.set(Calendar.MONTH, value);
//	cal.set(Calendar.DAY_OF_MONTH value);
    }

    public void commandAction(Command c, Displayable d) {
	if (c == MojMidlet2.powrot)
	    MojMidlet2.wyswietlacz.setCurrent(_parametry);

	else if (c == zapisz) {

	    try {
		MojMidlet2.records.deleteRecord(_id);
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	    try {
		writeStream(tfmodel.getString(), gram.getValue(), cgsim.getSelectedIndex(), _data);
		MojMidlet2.okno_prz = new Przegladanie(null, null, false);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    
	    MojMidlet2.wyswietlacz.setCurrent(new Alert("Zapisano!", tfmodel.getString(), null, AlertType.CONFIRMATION),
		    MojMidlet2.okno_prz);

	    tfmodel.delete(0, tfmodel.getString().length());
	    gram.setValue(5);
	    cgsim.setSelectedIndex(0, true);
	}
    }

    public static void writeStream(String sData, int iData, int iData2, String sData2)
	    throws IOException, RecordStoreException {
	ByteArrayOutputStream strmBytes = new ByteArrayOutputStream();
	DataOutputStream strmDataType = new DataOutputStream(strmBytes);

	byte[] record;

	strmDataType.writeUTF(sData);
	strmDataType.writeInt(iData);
	strmDataType.writeInt(iData2);
	strmDataType.writeUTF(sData2);

	// Clear any buffered data
	strmDataType.flush();

	// Get stream data into byte array and write record
	record = strmBytes.toByteArray();
	MojMidlet2.records.addRecord(record, 0, record.length);

	// Toss any data in the internal array so writes
	// starts at beginning (of the internal array)
	strmBytes.reset();

	strmBytes.close();
	strmDataType.close();
    }

}