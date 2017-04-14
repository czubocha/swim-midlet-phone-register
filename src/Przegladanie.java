import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordComparator;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordFilter;
import javax.microedition.rms.RecordStoreException;

public class Przegladanie extends List implements CommandListener {

    Command sortujMod;
    Command sortujRam;
    Command filtrujRam;
    RecordComparator _rc;
    RecordFilter _rf;
    boolean ramWidoczny;

    public Przegladanie(RecordComparator rc, RecordFilter rf, boolean ram) throws RecordStoreException, IOException {
	super("Baza telefonów:", List.IMPLICIT);
	_rc = rc;
	_rf = rf;
	sortujMod = new Command("Sortuj wg modeli", Command.ITEM, 0);
	sortujRam = new Command("Sortuj wg ramu", Command.ITEM, 0);
	filtrujRam = new Command("Filtruj...", Command.ITEM, 0);
	addCommand(MojMidlet2.powrot);
	addCommand(sortujMod);
	addCommand(sortujRam);
	addCommand(filtrujRam);
	setCommandListener(this);
	ramWidoczny = ram;
	listuj(rc, rf);
    }

    public void listuj(RecordComparator rc, RecordFilter rf) {
	try {
	    byte[] recData = new byte[100];

	    ByteArrayInputStream strmBytes = new ByteArrayInputStream(recData);
	    DataInputStream strmDataType = new DataInputStream(strmBytes);

	    RecordEnumeration it = MojMidlet2.records.enumerateRecords(rf, rc, false);

	    if (ramWidoczny)
		while (it.hasNextElement()) {
		    MojMidlet2.records.getRecord(it.nextRecordId(), recData, 0);
		    append(strmDataType.readUTF() + ", ram: " + strmDataType.readInt(), null);
		    strmBytes.reset();
		}
	    else {
		while (it.hasNextElement()) {
		    MojMidlet2.records.getRecord(it.nextRecordId(), recData, 0);
		    append(strmDataType.readUTF(), null);
		    strmBytes.reset();
		}
	    }

	    strmBytes.close();
	    strmDataType.close();

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public int wyszukaj() throws RecordStoreException, IOException {
	byte[] recData = new byte[100];
	ByteArrayInputStream strmBytes = new ByteArrayInputStream(recData);
	DataInputStream strmDataType = new DataInputStream(strmBytes);
	String model;
	String selected = getString(getSelectedIndex());
	if (ramWidoczny) {
	    model = selected.substring(0, selected.indexOf(","));
	} else {
	    model = selected;
	}
	RecordEnumeration it = MojMidlet2.records.enumerateRecords(null, null, false);
	int id = -1;
	int i;
	while (it.hasNextElement() && id == -1) {
	    i = it.nextRecordId();
	    MojMidlet2.records.getRecord(i, recData, 0);
	    strmDataType.reset();
	    String model2 = strmDataType.readUTF();
	    if (model.equals(model2))
		id = i;
	}
	return id;
    }

    public void commandAction(Command c, Displayable d) {

	if (c == MojMidlet2.powrot)
	    MojMidlet2.wyswietlacz.setCurrent(MojMidlet2.menu);
	else if (c == sortujMod)
	    try {
		MojMidlet2.okno_prz = new Przegladanie(new ModelComaparator(), null, false);
		MojMidlet2.wyswietlacz.setCurrent(MojMidlet2.okno_prz);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	else if (c == sortujRam)
	    try {
		MojMidlet2.okno_prz = new Przegladanie(new RamComaparator(), null, true);
		MojMidlet2.wyswietlacz.setCurrent(MojMidlet2.okno_prz);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	else if (c == filtrujRam) {
	    class PodajRam extends TextBox implements CommandListener {

		Command ok;

		public PodajRam(String title, String text, int maxSize, int constraints) {
		    super(title, text, maxSize, constraints);
		    ok = new Command("OK", Command.OK, 0);
		    addCommand(ok);
		    setCommandListener(this);
		}

		public void commandAction(Command c, Displayable d) {
		    if (c == ok) {
			try {
			    MojMidlet2.okno_prz = new Przegladanie(new RamComaparator(),
				    new RamFilter(Integer.parseInt(getString())), true);
			    MojMidlet2.wyswietlacz.setCurrent(MojMidlet2.okno_prz);
			} catch (Exception e) {
			    e.printStackTrace();
			}
		    }
		}
	    }
	    MojMidlet2.wyswietlacz.setCurrent(new PodajRam("Wprowadz min RAM", "", 1, 0));
	}

	else {
	    try {
		byte[] recData = new byte[100];
		ByteArrayInputStream strmBytes = new ByteArrayInputStream(recData);
		DataInputStream strmDataType = new DataInputStream(strmBytes);

		final int id = wyszukaj();
		strmDataType.reset();
		MojMidlet2.records.getRecord(id, recData, 0);
		final String model = strmDataType.readUTF();
		int ram = strmDataType.readInt();
		int sim = strmDataType.readInt();
		String str_sim = "";
		String data = strmDataType.readUTF();

		switch (sim) {
		case 0:
		    str_sim = "Mini-SIM";
		    break;
		case 1:
		    str_sim = "Micro-SIM";
		    break;
		case 2:
		    str_sim = "Nano-SIM";
		    break;
		}

		class Parametry extends TextBox implements CommandListener {

		    Command modify = new Command("Modyfikuj", Command.OK, 0);
		    Command usun = new Command("Usuñ", Command.OK, 0);

		    private Parametry(String model, int ram, String sim, String data)
			    throws IOException, RecordStoreException {
			super("Parametry telefonu",
				"Model: " + model + "\nRAM: " + ram + " GB\nSIM: " + sim + "\nData zakupu: " + data,
				256, TextField.UNEDITABLE);

			addCommand(MojMidlet2.powrot);
			addCommand(modify);
			addCommand(usun);
			setCommandListener(this);

		    }

		    public void commandAction(Command c, Displayable d) {
			if (c == MojMidlet2.powrot)
			    MojMidlet2.wyswietlacz.setCurrent(MojMidlet2.okno_prz);
			else if (c == modify) {
			    try {
				MojMidlet2.wyswietlacz.setCurrent(new Modyfikacja(id, this));
			    } catch (Exception e) {
			    }
			} else if (c == usun) {

			    try {
				MojMidlet2.records.deleteRecord(id);
			    } catch (Exception e) {
				e.printStackTrace();
			    }
			    try {
				MojMidlet2.wyswietlacz.setCurrent(
					new Alert("Usuniêto!", model, null, AlertType.CONFIRMATION),
					new Przegladanie(null, null, false));
			    } catch (Exception e) {
				e.printStackTrace();
			    }
			}

		    }
		}

		MojMidlet2.wyswietlacz.setCurrent(new Parametry(model, ram, str_sim, data));

	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

    class ModelComaparator implements RecordComparator {

	public int compare(byte[] rec1, byte[] rec2) {
	    ByteArrayInputStream strmBytes = new ByteArrayInputStream(rec1);
	    DataInputStream strmDataType = new DataInputStream(strmBytes);
	    String string1 = "", string2 = "";
	    try {
		string1 = strmDataType.readUTF();
		strmDataType.reset();
		strmBytes = new ByteArrayInputStream(rec2);
		strmDataType = new DataInputStream(strmBytes);
		string2 = strmDataType.readUTF();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    int comparison = string1.compareTo(string2);
	    if (comparison == 0) {
		return RecordComparator.EQUIVALENT;
	    } else if (comparison < 0) {
		return RecordComparator.PRECEDES;
	    } else {
		return RecordComparator.FOLLOWS;
	    }
	}

    }

    class RamComaparator implements RecordComparator {

	public int compare(byte[] rec1, byte[] rec2) {
	    ByteArrayInputStream strmBytes = new ByteArrayInputStream(rec1);
	    DataInputStream strmDataType = new DataInputStream(strmBytes);
	    int ram1 = -1, ram2 = -1;
	    try {
		strmDataType.readUTF();
		ram1 = strmDataType.readInt();
		strmDataType.reset();
		strmBytes = new ByteArrayInputStream(rec2);
		strmDataType = new DataInputStream(strmBytes);
		strmDataType.readUTF();
		ram2 = strmDataType.readInt();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    int comparison = ram1 - ram2;
	    if (comparison == 0) {
		return RecordComparator.EQUIVALENT;
	    } else if (comparison < 0) {
		return RecordComparator.PRECEDES;
	    } else {
		return RecordComparator.FOLLOWS;
	    }
	}
    }

    class RamFilter implements RecordFilter {

	int _limit;

	public RamFilter(int limit) {
	    _limit = limit;
	}

	public boolean matches(byte[] candidate) {
	    ByteArrayInputStream strmBytes = new ByteArrayInputStream(candidate);
	    DataInputStream strmDataType = new DataInputStream(strmBytes);
	    int ram = -1;
	    try {
		strmDataType.readUTF();
		ram = strmDataType.readInt();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    return (ram >= _limit) ? true : false;
	}

    }
}