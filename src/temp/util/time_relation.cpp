#include "ad3/FactorGraph.h"
#include <cstdlib>
#include "temp_util_TemporalDecoder.h"

void decode_graph(int num_arcs, const vector<double> &scores, const vector<vector<int> > &one_hot_constraints, const vector<vector<int> > &transitivity_constraints, vector<double> &posteriors){
  AD3::FactorGraph factor_graph;

  vector<AD3::BinaryVariable*> binary_variables(num_arcs);
  for (int i = 0; i < num_arcs; ++i) {
      binary_variables[i] = factor_graph.CreateBinaryVariable();
      binary_variables[i] ->SetLogPotential(scores[i]);
  }

  // Impose one-hot constraints.
  for (int i = 0; i < one_hot_constraints.size(); ++i){
    vector<AD3::BinaryVariable*> one_hot_variables;
    for (int j = 0; j < one_hot_constraints[i].size(); j++){
      one_hot_variables.push_back(binary_variables[one_hot_constraints[i][j]]);
    }
    factor_graph.CreateFactorXOR(one_hot_variables);
  }

  // Impose transitivity constraints.
  for (int i = 0; i < transitivity_constraints.size(); ++i){
    vector<AD3::BinaryVariable*> local_variables(3);
    local_variables[0] = binary_variables[transitivity_constraints[i][0]];
    local_variables[1] = binary_variables[transitivity_constraints[i][1]];
    local_variables[2] = binary_variables[transitivity_constraints[i][2]];
    factor_graph.CreateFactorIMPLY(local_variables);
  }

  vector<double> additional_posteriors;
  double value;

  // Run AD3.
  cout << "Running AD3..."
       << endl;
  factor_graph.SetEtaAD3(0.1);
  factor_graph.AdaptEtaAD3(true);
  factor_graph.SetMaxIterationsAD3(1000);
  //factor_graph.SolveExactMAPWithAD3(&posteriors, &additional_posteriors, &value);
  factor_graph.SolveLPMAPWithAD3(&posteriors, &additional_posteriors, &value);
}

JNIEXPORT jdouble JNICALL  Java_temp_util_TemporalDecoder_decode_1graph
  (JNIEnv *env, jobject thisObj, jobject j_scores, jobject j_oneHotConstraints, jobject j_transConstraints, jobject j_posteriors){
    jclass c_arraylist = env->FindClass("java/util/ArrayList");
    jmethodID fset_id = env->GetMethodID(c_arraylist,"set","(ILjava/lang/Object;)Ljava/lang/Object;");
    jmethodID fget_id = env->GetMethodID(c_arraylist,"get","(I)Ljava/lang/Object;");
    jmethodID fsize_id = env->GetMethodID(c_arraylist,"size","()I");

    jclass c_double = env->FindClass("java/lang/Double");
    jmethodID fdoublevalue_id = env->GetMethodID(c_double,"doubleValue","()D");

    jclass c_integer = env->FindClass("java/lang/Integer");
    jmethodID f_intvalue_id = env->GetMethodID(c_integer,"intValue","()I");
    jmethodID fdouble_init = env->GetMethodID(c_double, "<init>","(D)V");
    
    int num_arcs = env->CallIntMethod(j_scores, fsize_id);

    vector<double> scores;
    vector<vector<int> > one_hot_constraints;
    vector<vector<int> > transitivity_constraints; 

    vector<double> posteriors;

    for (int i = 0; i < num_arcs; ++i){
      jobject dobj = env->CallObjectMethod(j_scores, fget_id, i);
      double value = env->CallDoubleMethod(dobj, fdoublevalue_id);
      scores.push_back(value);
    }

    for (int i = 0; i < num_arcs; ++i){
      printf("%lf\n", scores[i]);
    }

    int size_onehot =  env->CallIntMethod(j_oneHotConstraints, fsize_id);

    for (int i = 0; i < size_onehot; ++i){
      jobject onehot_obj_i = env->CallObjectMethod(j_oneHotConstraints, fget_id, i);
      int size_onehot_i = env->CallIntMethod(onehot_obj_i, fsize_id);
      vector<int> onehot_i_vec;
      for (int j = 0; j < size_onehot_i; ++j){
        jobject iobj = env->CallObjectMethod(onehot_obj_i, fget_id, j);
        int v = env->CallIntMethod(iobj, f_intvalue_id);
        onehot_i_vec.push_back(v);
      }
      one_hot_constraints.push_back(onehot_i_vec);
    }

    int size_trans =  env->CallIntMethod(j_transConstraints, fsize_id);

    for (int i = 0; i < size_trans; ++i){
      jobject trans_obj_i = env->CallObjectMethod(j_transConstraints, fget_id, i);
      int size_trans_i = env->CallIntMethod(trans_obj_i, fsize_id);
      vector<int> trans_i_vec;
      for (int j = 0; j < size_trans_i; ++j){
        jobject iobj = env->CallObjectMethod(trans_obj_i, fget_id, j);
        int v = env->CallIntMethod(iobj, f_intvalue_id);
        trans_i_vec.push_back(v);
      }
      transitivity_constraints.push_back(trans_i_vec);
    }

    decode_graph(num_arcs, scores, one_hot_constraints, transitivity_constraints, posteriors);

    // for (int i = 0; i < posteriors.size(); ++i){
    //   cout << "posteriors[" << i << "]\t" << posteriors[i] << "\n";
    // }
    for (int i = 0; i < num_arcs; ++i){
      jobject value =  env->NewObject(c_double, fdouble_init, posteriors[i]);
      env->CallObjectMethod(j_posteriors, fset_id, i, value);
    }


}

// Test running
int main(int argc, char **argv) {
  int num_arcs = 18;

  double scores_arr[18] = {1.0, 0.0, 0.0, 0.0, 5.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

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

  vector<double> scores (scores_arr, scores_arr + sizeof(scores_arr) / sizeof(scores_arr[0]) );
  
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

  vector<double> posteriors;
  decode_graph(num_arcs, scores, one_hot_constraints, transitivity_constraints, posteriors);

  for (int i = 0; i < posteriors.size(); ++i){
    cout << "posteriors[" << i << "]\t" << posteriors[i] << "\n";
  }

  return 0;
}

