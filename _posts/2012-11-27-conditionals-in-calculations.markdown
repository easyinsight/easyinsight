---
title: "Conditionals in calculations"
author: "James Boe"
tags: ['scripting']
---
We've added conditional expressions to calculations so that you can now do:if ([Revenue] > 500, "X", "Y")orif ([Revenue] >= 500, colortext([Revenue], "#009900"))or so on! This syntax should be much cleaner than the previous greaterthan/lessthan/equalto that you had to use.<!--more-->