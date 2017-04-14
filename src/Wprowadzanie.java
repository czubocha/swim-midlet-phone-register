import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
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

public class Wprowadzanie extends Form implements CommandListener {

    Command zapisz;
    Image img;
    ImageItem imgItem;
    TextField model;
    Gauge ram;
    ChoiceGroup sim;
    DateField dataZakupu;

    public Wprowadzanie()
	    throws IOException, RecordStoreFullException, RecordStoreNotFoundException, RecordStoreException {
	super("Wprowadzanie");

	zapisz = new Command("Zapisz", Command.OK, 0);
	addCommand(MojMidlet2.powrot);
	addCommand(zapisz);
	setCommandListener(this);

	append("Wprowadz dane telefonu:");
	img = Image.createImage("/nexus3.jpg");
	append(img);

	model = new TextField("Model:", "", 256, TextField.ANY);
	append(model);
	ram = new Gauge("RAM:", true, 9, 5);
	append(ram);
	String[] simNames = { "Mini-SIM", "Micro-SIM", "Nano-SIM" };
	sim = new ChoiceGroup("Typ karty SIM:", ChoiceGroup.EXCLUSIVE, simNames, null);
	append(sim);
	dataZakupu = new DateField("Data zakupu:", DateField.DATE);
	append(dataZakupu);
    }

    public void commandAction(Command c, Displayable d) {
	if (c == MojMidlet2.powrot)
	    MojMidlet2.wyswietlacz.setCurrent(MojMidlet2.menu);

	else if (c == zapisz) {

	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(dataZakupu.getDate());
	    String data = new String(calendar.get(Calendar.DAY_OF_MONTH) + "." + calendar.get(Calendar.MONTH) + "."
		    + calendar.get(Calendar.YEAR));

	    try {
		writeStream(model.getString(), ram.getValue(), sim.getSelectedIndex(), data);
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	    MojMidlet2.wyswietlacz.setCurrent(new Alert("Zapisano!", model.getString(), null, AlertType.CONFIRMATION),
		    MojMidlet2.menu);

	    model.delete(0, model.getString().length());
	    ram.setValue(5);
	    sim.setSelectedIndex(0, true);

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