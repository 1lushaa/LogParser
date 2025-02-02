package backend.academy.analyzer.args;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class OffsetDateTimeConverter implements IStringConverter<OffsetDateTime> {

    @Override
    public OffsetDateTime convert(String time) {
        try {
            return OffsetDateTime.parse(time);
        } catch (DateTimeParseException e) {
            throw new ParameterException("Invalid date or date format: \"" + time + "\"");
        }
    }
}
