package ru.csc.bdse.coordinator;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.csc.bdse.controller.KeyValueApiController;

@RestController
@RequestMapping("coordinator")
public class CoordinatorController extends KeyValueApiController {
    public CoordinatorController(CoordinatorConfig config) {
        super(new CoordinatorKeyValueApi(
                config.apis().values(),
                config.timeoutMills(),
                config.writeConsistencyLevel(),
                config.readConsistencyLevel()
        ));
    }
}
