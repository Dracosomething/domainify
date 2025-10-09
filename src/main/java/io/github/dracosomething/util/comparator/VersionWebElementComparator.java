package io.github.dracosomething.util.comparator;

import org.openqa.selenium.WebElement;

public class VersionWebElementComparator extends VersionComparator<WebElement> {
    public VersionWebElementComparator(String fileName, String fileExtension) {
        super(fileName, fileExtension);
    }

    @Override
    public String format(WebElement object) {
        String text = object.getText();
        if (object.getAttribute("href") != null) {
            text = object.getAttribute("href");
        }
        if (text == null) {
            text = "";
        }
        return text.replaceFirst("((https|http)://.*\\..*\\..*|)/.*/", "");
    }
}
