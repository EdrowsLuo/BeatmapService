package com.edlplan.audiov.core.option;

import java.util.Map;

public interface IHasOption {

    Map<String, OptionEntry<?>> dumpOptions();

    void applyOptions(Map<String, OptionEntry<?>> options);

}
