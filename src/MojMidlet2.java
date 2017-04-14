import java.io.IOException;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;

public class MojMidlet2 extends MIDlet implements CommandListener {

    static Display wyswietlacz;
    static List menu;
    Command koniec;
    Command kasuj;
    static Command powrot;
    static Form okno_wpr;
    static RecordStore records;
    static List okno_prz;

    public MojMidlet2()
	    throws RecordStoreFullException, RecordStoreNotFoundException, RecordStoreException, IOException {
	wyswietlacz = Display.getDisplay(this);
	records = RecordStore.openRecordStore("telefony", true);
	String[] elements = { "Wprowadzanie danych", "Przegl¹danie danych" };
	menu = new List("Menu", List.IMPLICIT, elements, null);
	powrot = new Command("Powrót", Command.BACK, 0);
	koniec = new Command("Koniec", Command.EXIT, 0);
	kasuj = new Command("Skasuj bazê", Command.ITEM, 0);
	menu.addCommand(koniec);
	menu.addCommand(kasuj);

	okno_wpr = new Wprowadzanie();
    }

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
    }

    protected void pauseApp() {
    }

    protected void startApp() throws MIDletStateChangeException {
	wyswietlacz.setCurrent(menu);
	menu.setCommandListener(this);
    }

    public void commandAction(Command komenda, Displayable elemEkranu) {
	List down = (List) wyswietlacz.getCurrent();
	if (komenda == koniec)
	    notifyDestroyed();
	else if (komenda == kasuj) {
	    MojMidlet2.wyswietlacz.setCurrent(new Alert("Uwaga!", "Ta operacja wyma¿e Twoje dane", null, AlertType.WARNING));
	    try {
		records.closeRecordStore();
		RecordStore.deleteRecordStore("telefony");
		records = RecordStore.openRecordStore("telefony", true);
		MojMidlet2.wyswietlacz.setCurrent(new Alert("Skasowano!"));
	    } catch (RecordStoreException e) {
		e.printStackTrace();
	    }
	} else
	    switch (down.getSelectedIndex()) {
	    case 0:
		wyswietlacz.setCurrent(okno_wpr);
		break;
	    case 1:
		try {
		    okno_prz = new Przegladanie(null, null, false);
		} catch (Exception e) {
		    e.printStackTrace();
		}
		wyswietlacz.setCurrent(okno_prz);
		break;
	    }
    }
}
