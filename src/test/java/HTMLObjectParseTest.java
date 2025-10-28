import io.github.dracosomething.util.FileUtils;
import io.github.dracosomething.util.HTMLObject;
import io.github.dracosomething.util.Logger;
import org.junit.jupiter.api.Test;

public class HTMLObjectParseTest {
    @Test
    public void HTMLObject_parse_test() {
        HTMLObject test = HTMLObject.parse("<div class=\"instructions-row\">" +
                "        I want to use PHP for" +
                "        <select id=\"usage\" name=\"usage\">" +
                "                            <option value=\"web\" selected=\"\">Web Development</option>                            <option value=\"cli\">CLI/Library Development</option>                            <option value=\"fw-drupal\">Drupal Development</option>                            <option value=\"fw-joomla\">Joomla Development</option>                            <option value=\"fw-laravel\">Laravel Development</option>                            <option value=\"fw-symfony\">Symfony Development</option>                            <option value=\"fw-wordpress\">WordPress Development</option>                    </select>.\n" +
                "    </div>");
    }

    @Test
    public void logger_test() {
        Logger logger = Logger.getLogger("test");
        logger.log("ewrer", Logger.PrintColor.WHITE, Logger.LogType.INFO);
    }
}
