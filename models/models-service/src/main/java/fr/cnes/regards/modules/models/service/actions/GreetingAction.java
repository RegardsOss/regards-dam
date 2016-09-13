/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service.actions;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

import fr.cnes.regards.modules.jobs.Job;
import fr.cnes.regards.modules.jobs.StatusInfo;
import fr.cnes.regards.modules.models.domain.Greeting;

/**
 * 
 * TODO Description
 * @author TODO
 *
 */
public class GreetingAction extends Job {

    private static final String template = "Hello, %s!";

    private final AtomicLong counter = new AtomicLong();

    private final String name_;

    public GreetingAction(String pName) {
        super();
        name_ = pName;
    }

    @Override
    public StatusInfo cancel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StatusInfo execute() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasResult() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean needWorkspace() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public StatusInfo restart() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StatusInfo stop() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setWorkspace(Path pPath) {
        // TODO Auto-generated method stub

    }

}
