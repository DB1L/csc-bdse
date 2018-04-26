package ru.csc.bdse.partitioning;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.csc.bdse.controller.KeyValueApiController;

@RestController
@RequestMapping("partition")
public class PartitionController extends KeyValueApiController {
    public PartitionController(PartitionConfig config) {
        super(new PartitionedKeyValueApi(config.partitions(), config.timeoutMillis(), config.partitioner()));
    }
}
