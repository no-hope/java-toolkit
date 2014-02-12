package foo;

import org.jvnet.jax_ws_commons.json.JSONBindingID;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingType;
import java.util.*;

/**
 * targetNamespace="http://jax-ws.dev.java.net/json" is a must.
 *
 * @author Jitendra Kotamraju
 */
@WebService(targetNamespace = "http://jax-ws.dev.java.net/json")
@BindingType(JSONBindingID.JSON_BINDING)
public class MyService {

    public static XMLGregorianCalendar toXmlCalendar(final Date date, final String timezoneId) {
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone(timezoneId));
        calendar.setTime(date);

        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
        } catch (final DatatypeConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    public Book get(@WebParam(name="p1") final int p1
            , @WebParam(name="p2") final String p2
            , @WebParam(name="p3") final XMLGregorianCalendar p3
            , @WebParam(name="p4") final List<Sub> p4
    ) {
        /*System.err.println("********* "+p3);
        System.err.println("********* "+ JSON.JSON.pretty(p4));
        */
        return new Book(p1,p2, p3);
    }

    public static final class Sub2 {
        public int sf1 = 2;
        public String sf2 = "zzz";

        public Sub2() {
        }
    }

    public static final class Sub {
        public int f1 = 1;
        public String f2 = "xxx";
        public List<Sub2> f3 = new ArrayList<>();

        public Sub() {
        }
    }

    public static final class Book {
        public int id = 1;
        public String title = "Java";
        public int p1;
        public String p2;
        public String nullValue = null;
        public float floatValue = 1.23456f;
        public double doubleValue = 1.23456;
        public boolean booleanValue = true;
        public List<Sub> subList = new ArrayList<>();

        @XmlElement(name = "calendar", required = true)
        @XmlSchemaType(name = "dateTime")
        public XMLGregorianCalendar calendar; // = toXmlCalendar(new Date(), "UTC");

        public Book() {
            subList.add(new Sub());
        }

        public Book(final int p1, final String p2, final XMLGregorianCalendar calendar) {
            this.p1 = p1;
            this.p2 = p2;
            this.calendar = calendar;
            subList.add(new Sub());
        }
    }

}
