package nl.thehyve.hypercubeapi.observation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.transmartproject.common.dto.Query;
import org.transmartproject.common.resource.ObservationServer;

import javax.validation.Valid;

@RestController
@Validated
@CrossOrigin
public class ObservationEndpoint implements ObservationServer {

    private final HypercubeService hypercubeService;

    @Autowired
    ObservationEndpoint(HypercubeService hypercubeService) {
        this.hypercubeService = hypercubeService;
    }

    @Override
    public ResponseEntity<StreamingResponseBody> query(@Valid Query query) {
        StreamingResponseBody stream = output -> {
            this.hypercubeService.write(output, query.getConstraint());
        };
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(stream);
    }

}
