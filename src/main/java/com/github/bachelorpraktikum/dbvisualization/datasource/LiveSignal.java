package com.github.bachelorpraktikum.dbvisualization.datasource;

import com.github.bachelorpraktikum.dbvisualization.model.Context;
import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

class LiveSignal {

    @SerializedName("hs")
    private String haupt;
    @SerializedName("vs")
    private String vor;
    @SerializedName("sp")
    private String sicht;
    private String name;

    public boolean contains(Element element) {
        String elementName = element.getName();
        return elementName.equals(haupt)
            || elementName.equals(vor)
            || elementName.equals(sicht);
    }

    public List<Element> getElements(Context context) {
        List<Element> result = new ArrayList<>(3);
        addElement(haupt, context, result);
        addElement(vor, context, result);
        addElement(sicht, context, result);
        return result;
    }

    private void addElement(String name, Context context, List<Element> result) {
        if (name != null) {
            result.add(Element.in(context).get(name));
        }
    }


    public String getName() {
        return name;
    }

}
