package it.at.restfs.event;

import java.util.List;
import java.util.UUID;
import com.google.inject.Inject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ContainerEvents {

    private final UUID container;
    private final List<Event> events;
    
}
