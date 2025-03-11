# Smart AI plugin
Just a toy project to show my skills building a plugin.

### The assignment
1. Define the scope of problems in the projects you want to address using LLM
2. Implement an inspection that spots the most complex and hard to maintain parts of the
   project.
3. Collect the required context and form the prompt
4. Implement the client to access OpenAI API https://openai.com/index/openai-api/ . and
   provide a quick-fix that addresses the problems found with the inspection with prompt(s)
   formed on the step
5. For found smelly code, provide the quickfix that 
   1. It shows the UI (it could be a simple dialog that shows the intermediate steps) to
      track the progress in the context and send a request to LLM.
   2. Apply the fix to all affected files.
6. Design the metrics service that collects the statistics/logs on the history of the interaction
   with the inspection
7. Give some insights on the metrics outcomes of the implementation and how the
   improvements influenced it.

### Solution
As a solution, the following inspections were implemented:
* Dummy name inspection - it searches for dummy names in fields, variables and methods and proposes a
  better name based on context. Some notable limitations:
  * It works with java and kotlin only now (this is because of some code postprocessing after the LLM response)
  * Only local inspection (on the fly in the editor)

### Metrics
TODO