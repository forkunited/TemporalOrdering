// Copyright (c) 2012 Andre Martins
// All Rights Reserved.
//
// This file is part of AD3 2.0.
//
// AD3 2.0 is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// AD3 2.0 is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with AD3 2.0.  If not, see <http://www.gnu.org/licenses/>.
//

#include "ad3/FactorGraph.h"
#include <cstdlib>

int main(int argc, char **argv) {
  /*
    TLink:t14->ei100AFTER
    TLink:t14->ei100INCLUDES
    TLink:ei99->ei100VAGUE
    TLink:t14->ei100SIMULTANEOUS
    TLink:ei99->ei100BEFORE
    TLink:t14->ei100BEFORE
    TLink:ei99->ei100IS_INCLUDED
    TLink:ei99->ei100SIMULTANEOUS
    TLink:ei99->ei100AFTER
    TLink:ei99->t14AFTER
    TLink:ei99->t14INCLUDES
    TLink:ei99->t14VAGUE
    TLink:ei99->ei100INCLUDES
    TLink:ei99->t14BEFORE
    TLink:t14->ei100VAGUE
    TLink:ei99->t14SIMULTANEOUS
    TLink:ei99->t14IS_INCLUDED
    TLink:t14->ei100IS_INCLUDED
  */

  int num_arcs = 18;

  double scores[18] = {1.0, 0.0, 0.0, 0.0, 5.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

  vector<vector<int> > one_hot_constraints(3);

  one_hot_constraints[0].push_back(1);
  one_hot_constraints[0].push_back(0);
  one_hot_constraints[0].push_back(17);
  one_hot_constraints[0].push_back(14);
  one_hot_constraints[0].push_back(3);
  one_hot_constraints[0].push_back(5);

  one_hot_constraints[1].push_back(10);
  one_hot_constraints[1].push_back(9);
  one_hot_constraints[1].push_back(16);
  one_hot_constraints[1].push_back(11);
  one_hot_constraints[1].push_back(15);
  one_hot_constraints[1].push_back(13);

  one_hot_constraints[2].push_back(12);
  one_hot_constraints[2].push_back(8);
  one_hot_constraints[2].push_back(6);
  one_hot_constraints[2].push_back(2);
  one_hot_constraints[2].push_back(7);
  one_hot_constraints[2].push_back(4);


  /*
    [15, 5, 4]
    [15, 3, 7]
    [16, 0, 8]
    [13, 5, 4]
    [9, 3, 8]
    [15, 1, 12]
    [16, 17, 6]
    [9, 0, 8]
    [10, 3, 12]
    [10, 1, 12]
    [15, 17, 6]
    [13, 1, 4]
    [9, 1, 8]
    [13, 3, 4]
    [16, 3, 6]
    [16, 5, 4]
    [15, 0, 8]
  */
  vector<vector<int> > transitivity_constraints(17); 
  for(int i = 0; i < transitivity_constraints.size(); i++){
    transitivity_constraints[i].resize(3);
  }
  transitivity_constraints[0][0] = 15;
  transitivity_constraints[0][1] = 5;
  transitivity_constraints[0][2] = 4;

  transitivity_constraints[1][0] = 15;
  transitivity_constraints[1][1] = 3;
  transitivity_constraints[1][2] = 7;

  transitivity_constraints[2][0] = 16;
  transitivity_constraints[2][1] = 0;
  transitivity_constraints[2][2] = 8;

  transitivity_constraints[3][0] = 13;
  transitivity_constraints[3][1] = 5;
  transitivity_constraints[3][2] = 4;

  transitivity_constraints[4][0] = 9;
  transitivity_constraints[4][1] = 3;
  transitivity_constraints[4][2] = 8;

  transitivity_constraints[5][0] = 15;
  transitivity_constraints[5][1] = 1;
  transitivity_constraints[5][2] = 12;

  transitivity_constraints[6][0] = 16;
  transitivity_constraints[6][1] = 17;
  transitivity_constraints[6][2] = 6;

  transitivity_constraints[7][0] = 9;
  transitivity_constraints[7][1] = 0;
  transitivity_constraints[7][2] = 8;

  transitivity_constraints[8][0] = 10;
  transitivity_constraints[8][1] = 3;
  transitivity_constraints[8][2] = 12;

  transitivity_constraints[9][0] = 10;
  transitivity_constraints[9][1] = 1;
  transitivity_constraints[9][2] = 12;

  transitivity_constraints[10][0] = 15;
  transitivity_constraints[10][1] = 17;
  transitivity_constraints[10][2] = 6;

  transitivity_constraints[11][0] = 13;
  transitivity_constraints[11][1] = 1;
  transitivity_constraints[11][2] = 4;

  transitivity_constraints[12][0] = 9;
  transitivity_constraints[12][1] = 1;
  transitivity_constraints[12][2] = 8;

  transitivity_constraints[13][0] = 13;
  transitivity_constraints[13][1] = 3;
  transitivity_constraints[13][2] = 4;

  transitivity_constraints[14][0] = 16;
  transitivity_constraints[14][1] = 3;
  transitivity_constraints[14][2] = 6;

  transitivity_constraints[15][0] = 16;
  transitivity_constraints[15][1] = 5;
  transitivity_constraints[15][2] = 4;

  transitivity_constraints[16][0] = 15;
  transitivity_constraints[16][1] = 0;
  transitivity_constraints[16][2] = 8;




  AD3::FactorGraph factor_graph;

  vector<AD3::BinaryVariable*> binary_variables(num_arcs);
  for (int i = 0; i < num_arcs; ++i) {
      binary_variables[i] = factor_graph.CreateBinaryVariable();
      binary_variables[i] ->SetLogPotential(scores[i]);
  }


  for (int i = 0; i < one_hot_constraints.size(); ++i){
    vector<AD3::BinaryVariable*> one_hot_variables;
    for (int j = 0; j < one_hot_constraints[i].size(); j++){
      one_hot_variables.push_back(binary_variables[one_hot_constraints[i][j]]);
    }
    factor_graph.CreateFactorXOR(one_hot_variables);
  }
  
  // binary_variables[0] ->SetLogPotential(100);
  // binary_variables[1] ->SetLogPotential(200);
  // binary_variables[2] ->SetLogPotential(-50);

  // Impose transitivity constraints.
  for (int i = 0; i < transitivity_constraints.size(); ++i){
    vector<AD3::BinaryVariable*> local_variables(3);
    local_variables[0] = binary_variables[transitivity_constraints[i][0]];
    local_variables[1] = binary_variables[transitivity_constraints[i][1]];
    local_variables[2] = binary_variables[transitivity_constraints[i][2]];
    factor_graph.CreateFactorIMPLY(local_variables);
  }
  
  vector<double> posteriors;
  vector<double> additional_posteriors;
  double value;

  // Run AD3.
  cout << "Running AD3..."
       << endl;
  factor_graph.SetEtaAD3(0.1);
  factor_graph.AdaptEtaAD3(true);
  factor_graph.SetMaxIterationsAD3(1000);
  factor_graph.SolveLPMAPWithAD3(&posteriors, &additional_posteriors, &value);

  for (int i = 0; i < posteriors.size(); ++i){
    cout << "posteriors[" << i << "]\t" << posteriors[i] << "\n";
  }

  return 0;
}

