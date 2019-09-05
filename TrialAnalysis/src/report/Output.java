package report;

import vat.Vat;
import data.xml.objects.Trial;

public interface Output {
	public void runOutput(Vat vat, Trial trial,ReportOutput reportOutput);
}
