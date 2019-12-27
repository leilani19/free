package org.odk.collect.android.instances;

import java.util.ArrayList;
import java.util.List;

public final class TestInstancesRepository implements InstancesRepository {
    List<Instance> instances;

    public TestInstancesRepository(List<Instance> instances) {
        this.instances = new ArrayList<>(instances);
    }

    @Override
    public List<Instance> getAllBy(String formId) {
        List<Instance> result = new ArrayList<>();

        for (Instance instance : instances) {
            if (instance.getJrFormId().equals(formId)) {
                result.add(instance);
            }
        }

        return result;
    }

    @Override
    public Instance getByPath(String instancePath) {
        List<Instance> result = new ArrayList<>();

        for (Instance instance : instances) {
            if (instance.getInstanceFilePath().equals(instancePath)) {
                result.add(instance);
            }
        }

        if (result.size() == 1) {
            return result.get(0);
        } else {
            return null;
        }
    }

    public void addInstance(Instance instance) {
        instances.add(instance);
    }

    public void removeInstanceById(int databaseId) {
        for (int i = 0; i < instances.size(); i++) {
            if (instances.get(i).getDatabaseId() == databaseId) {
                instances.remove(i);
                return;
            }
        }
    }
}
