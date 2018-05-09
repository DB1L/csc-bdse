package ru.csc.bdse.partitioning;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.csc.bdse.controller.KeyValueApiController;
import ru.csc.bdse.coordinator.Config;

@RestController
@RequestMapping("partition")
public class PartitionController extends KeyValueApiController {
    public PartitionController(Config config) {
        super(new PartitionedKeyValueApi(config.apis(), config.timeoutMills(), config.partitioner()));
    }
}

