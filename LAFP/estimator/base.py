import os
import inspect
import estimator.supported_framework as sf

def estimator(csv_paths):
    #Returns which framework to use for execution

    #Parameters:
    #    csv_paths: List of input paths

    #Returns:
    #    enum SupportedFrameworks: Identified framework, As of now this returns Pandas by default
    abs_path = os.path.abspath((inspect.stack()[1])[1])
    return abs_path
    #return sf.SupportedFrameworks.PANDAS