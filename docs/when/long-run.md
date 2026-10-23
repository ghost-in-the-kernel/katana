# When you start a run whose output is long (R-076)

The gate (`scripts/prepush.sh`) and `gradle integration` print thousands of lines. Write the output to a file
under `build/`, and read only the verdict and the failures:

    scripts/prepush.sh > build/gate.log 2>&1; grep "pre-push check:" build/gate.log

The gate's verdict is `pre-push check: passed` or `pre-push check: FAILED`; grep for `pre-push check:`, never one
case of it. A sub-agent you run does the same and reports only those lines.
