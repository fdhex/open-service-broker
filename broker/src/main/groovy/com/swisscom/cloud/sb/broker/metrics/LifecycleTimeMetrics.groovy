package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.Metric
import org.springframework.stereotype.Service

@Service
@CompileStatic
class LifecycleTimeMetrics extends ServiceBrokerMetrics {

    private final String LIFECYCLE_TIME = "lifecycleTime"

    private HashMap<String, Long> totalLifecycleTimePerService
    private HashMap<String, Long> totalNrOfDeleteInstancesPerService
    private HashMap<String, Long> meanLifecycleTimePerService = new HashMap<>()

    @Autowired
    LifecycleTimeMetrics(ServiceInstanceRepository serviceInstanceRepository, LastOperationRepository lastOperationRepository) {
        super(serviceInstanceRepository, lastOperationRepository)
    }

    void calculateLifecycleTimePerService() {
        HashMap<String, Long> total = new HashMap<>()
        HashMap<String, Long> totalLifecycleTime = new HashMap<>()

        def list = serviceInstanceRepository.findAll()
        list.findAll { instance -> instance.deleted }.each {
            serviceInstance ->
                def serviceName = getServiceName(serviceInstance)
                total = addEntryToHm(total, serviceName)
                totalLifecycleTime = addUpLifecycleTime(totalLifecycleTime, serviceName, serviceInstance)
        }
        totalNrOfDeleteInstancesPerService = total
        totalLifecycleTimePerService = totalLifecycleTime
        calculateMeanLifecycleTime()
    }

    HashMap<String, Long> addUpLifecycleTime(HashMap<String, Long> hm, String key, ServiceInstance serviceInstance) {
        def dateCreated = serviceInstance.dateCreated.getTime()
        def dateDeleted = serviceInstance.dateDeleted.getTime()
        if (dateCreated != null && dateDeleted != null) {
            def lifecycleTime = dateDeleted - dateCreated
            if (hm.get(key) == null) {
                hm.put(key, lifecycleTime)
            } else {
                def currentValue = hm.get(key)
                def newValue = currentValue + lifecycleTime
                hm.put(key, newValue)
            }
        }
        return hm
    }

    void calculateMeanLifecycleTime() {
        totalNrOfDeleteInstancesPerService.findAll { service ->
            def serviceName = service.getKey()
            def totalNrOfInstances = service.getValue()
            def totalLifecycleTime = totalLifecycleTimePerService.get(serviceName)
            def meanLifecycleTime = (totalLifecycleTime / totalNrOfInstances).toLong()
            meanLifecycleTimePerService.put(serviceName, meanLifecycleTime)
        }
    }

    @Override
    Collection<Metric<?>> metrics() {
        List<Metric<?>> metrics = new ArrayList<>()

        calculateLifecycleTimePerService()
        metrics = addCountersFromHashMapToMetrics(meanLifecycleTimePerService, meanLifecycleTimePerService, metrics, LIFECYCLE_TIME, SERVICE, TOTAL)
        return metrics
    }

    @Override
    boolean considerServiceInstance(ServiceInstance serviceInstance) {
        return false
    }

    @Override
    String tag() {
        return LifecycleTimeMetrics.class.getSimpleName()
    }
}
