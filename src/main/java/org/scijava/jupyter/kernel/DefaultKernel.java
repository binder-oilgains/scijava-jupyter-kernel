/* 
 * Copyright 2017 Hadrien Mary.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scijava.jupyter.kernel;


import com.twosigma.beaker.jupyter.handler.CommOpenHandler;
import com.twosigma.jupyter.KernelRunner;
import com.twosigma.jupyter.handler.KernelHandler;
import com.twosigma.jupyter.message.Message;
import com.twosigma.jupyter.Kernel;

import java.io.IOException;

import static com.twosigma.beaker.jupyter.Utils.uuid;
import com.twosigma.jupyter.KernelSocketsFactoryImpl;
import org.scijava.Context;
import org.scijava.jupyter.DefaultKernelConfigurationFile;
import org.scijava.jupyter.comm.DefaultCommOpenHandler;
import org.scijava.jupyter.evaluator.DefaultEvaluator;
import org.scijava.jupyter.handler.DefaultKernelInfoHandler;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.script.ScriptLanguage;

/**
 *
 * @author Hadrien Mary
 */
public class DefaultKernel extends Kernel {

    // Scijava context
    Context context;

    @Parameter
    private transient LogService log;

    // Ugly but needed (should be fixed upstream soon)
    private static ScriptLanguage scriptLanguage;

    private final DefaultKernelConfigurationFile config;
    private final DefaultEvaluator evaluator;

    public DefaultKernel(final Context context, final String id, final DefaultEvaluator evaluator,
            DefaultKernelConfigurationFile config, KernelSocketsFactoryImpl kernelSocketsFactory) {

        super(id, evaluator, kernelSocketsFactory);
        this.context = context;
        this.context.inject(this);
        this.config = config;
        this.evaluator = evaluator;

        log.info("Scijava Kernel started.");
        log.info("Language used : " + this.config.getLanguageName());

        this.setLogLevel(config.getLogLevel());
        log.info("Log level used is : " + this.config.getLogLevel());
    }

    @Override
    public CommOpenHandler getCommOpenHandler(Kernel kernel) {
        return new DefaultCommOpenHandler(kernel);
    }

    @Override
    public KernelHandler<Message> getKernelInfoHandler(Kernel kernel) {
        return new DefaultKernelInfoHandler(kernel, DefaultKernel.scriptLanguage);
    }

    private void setLogLevel(String logLevel) {
        switch (logLevel) {
            case "debug":
                this.log.setLevel(LogService.DEBUG);
                break;
            case "error":
                this.log.setLevel(LogService.ERROR);
                break;
            case "info":
                this.log.setLevel(LogService.INFO);
                break;
            case "none":
                this.log.setLevel(LogService.NONE);
                break;
            default:
                this.log.setLevel(LogService.INFO);
                break;
        }
    }

    public static void main(final String[] args) throws InterruptedException, IOException {

        // TODO : Should this be a Scijava command ?
        KernelRunner.run(() -> {

            Context context = new Context();
            String id = uuid();

            DefaultKernelConfigurationFile config = new DefaultKernelConfigurationFile(context, args);
            KernelSocketsFactoryImpl kernelSocketsFactory = new KernelSocketsFactoryImpl(config);
            DefaultEvaluator evaluator = new DefaultEvaluator(context, id, id, config.getLanguageName());

            DefaultKernel.scriptLanguage = evaluator.getScriptLanguage();

            return new DefaultKernel(context, id, evaluator, config, kernelSocketsFactory);
        });
    }
}
