package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.BaseData.CalculateOperator;
import com.cyzapps.Jfcalc.BaseData.CurPos;
import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.BaseData.OPERATORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr.ABSTRACTEXPRTYPES;
import com.cyzapps.Jsma.AbstractExpr.SimplifyParams;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.PatternManager.ABSTRACTEXPRPATTERNS;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;

public class PtnSlvMultiVarsIdentifier {
	
	public static final String FUNCTION_ORIGINAL_EXPRESSION = "f_aexpr_to_analyze";
			
	static public class PatternSetByString
	{
		public String mstrPattern = "";
		public String[] mstrarrayPseudoConsts = new String[0];
		public String[] mstrarrayPConExprs = new String[0];
	}
	
	static public class PatternSetByAExpr
	{
		public AbstractExpr maePattern = AEInvalid.AEINVALID;
		public AEVar[] maePseudoConsts = new AEVar[0];
		public AbstractExpr[] maePConExprs = new AbstractExpr[0];
	}
		
	public ABSTRACTEXPRPATTERNS menumAEPType = ABSTRACTEXPRPATTERNS.AEP_UNRECOGNIZEDPATTERN;
	
	public PatternSetByString[] mpssArray = new PatternSetByString[0];
	public String[] mstrarrayVars = new String[0];	// note that all variables have to appear in each pattern
	public int[] mnarrayVarOrders = new int[0];
	public String[] mstrarraySolveVarExprs = new String[0];
	public boolean[] mbarrayRootsList = new boolean[0];	// if is true, the returned array is actually a list of roots for this var.
	
	public PatternSetByAExpr[] mpsaArray = new PatternSetByAExpr[0];
	public AEVar[] marrayaeVars = new AEVar[0];
	public AbstractExpr[] marrayaeSolveVarExprs = new AbstractExpr[0];
	
	public PtnSlvMultiVarsIdentifier()	{
		
	}
	
	public PtnSlvMultiVarsIdentifier(ABSTRACTEXPRPATTERNS enumAEPType,
					PatternSetByString[] pssArray,
					String[] strarrayVars,
					int[] narrayVarOrders,
					String[] strarraySolveVarExprs,
					boolean[] barrayRootsList)
					throws JSmartMathErrException, JFCALCExpErrException, InterruptedException	{
		setPtnSlvMultiVarsIdentifier(enumAEPType, pssArray, strarrayVars, narrayVarOrders, strarraySolveVarExprs, barrayRootsList);
	}
	
	public void setPtnSlvMultiVarsIdentifier(ABSTRACTEXPRPATTERNS enumAEPType,
					PatternSetByString[] pssArray,
					String[] strarrayVars,
					int[] narrayVarOrders,
					String[] strarraySolveVarExprs,
					boolean[] barrayRootsList)
					throws JSmartMathErrException, JFCALCExpErrException, InterruptedException	{
		menumAEPType = enumAEPType;
		
		mpssArray = pssArray;
		mstrarrayVars = strarrayVars;
		mnarrayVarOrders = narrayVarOrders;
		mstrarraySolveVarExprs = strarraySolveVarExprs;
		mbarrayRootsList = barrayRootsList;
		
		mpsaArray = new PatternSetByAExpr[mpssArray.length];
		marrayaeVars = new AEVar[mstrarrayVars.length];
		marrayaeSolveVarExprs = new AbstractExpr[mstrarrayVars.length];
		for (int idx = 0; idx < mpssArray.length; idx ++)	{
			if (mpssArray[idx].mstrarrayPConExprs.length != mpssArray[idx].mstrarrayPseudoConsts.length)	{
				throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPRPATTERN);
			}
			mpsaArray[idx] = new PatternSetByAExpr();
			mpsaArray[idx].maePConExprs = new AbstractExpr[mpssArray[idx].mstrarrayPConExprs.length];
			mpsaArray[idx].maePseudoConsts = new AEVar[mpssArray[idx].mstrarrayPseudoConsts.length];
		}
		
		if (strarrayVars.length != narrayVarOrders.length)	{
			throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPRPATTERN);
		}
		
		LinkedList<Variable> listPseudoConsts = new LinkedList<Variable>();
		for (int idx = 0; idx < mpssArray.length; idx ++)	{
			for (int idx1 = 0; idx1 < mpssArray[idx].mstrarrayPseudoConsts.length; idx1 ++)	{
				AbstractExpr aexprTmp = ExprAnalyzer.analyseExpression(mpssArray[idx].mstrarrayPseudoConsts[idx1], new CurPos());
				if (!(aexprTmp instanceof AEVar))	{
					throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPRPATTERN);
				}
				mpsaArray[idx].maePseudoConsts[idx1] = (AEVar) aexprTmp;
				mpsaArray[idx].maePseudoConsts[idx1].menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_PSEUDOCONST;
				if (VariableOperator.lookUpList(
						((AEVar)mpsaArray[idx].maePseudoConsts[idx1]).mstrVariableName,
						listPseudoConsts) == null)	{
					listPseudoConsts.add(new Variable(((AEVar)mpsaArray[idx].maePseudoConsts[idx1]).mstrVariableName));
				}
				mpsaArray[idx].maePConExprs[idx1] = ExprAnalyzer.analyseExpression(mpssArray[idx].mstrarrayPConExprs[idx1], new CurPos());		
			}
		}
		for (int idx = 0; idx < mstrarrayVars.length; idx ++)	{
			AbstractExpr aexprTmp = ExprAnalyzer.analyseExpression(mstrarrayVars[idx], new CurPos());
			if (!(aexprTmp instanceof AEVar))	{
				throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPRPATTERN);
			}
			marrayaeVars[idx] = (AEVar) aexprTmp;
			if (mnarrayVarOrders[idx] != 0)	{
				// so far all the pattern variables have the same priority, ie they can reorder.
				throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_PATTERN_VARIABLE_ORDER);
			}
			marrayaeSolveVarExprs[idx] = ExprAnalyzer.analyseExpression(mstrarraySolveVarExprs[idx], new CurPos());
		}
		for (int idx = 0; idx < mpssArray.length; idx ++)	{
			mpsaArray[idx].maePattern = ExprAnalyzer.analyseExpression(mpssArray[idx].mstrPattern, new CurPos(), listPseudoConsts);
		}
	}
	
	/*
	 * x and x[1] are the same to solved variable, x[y] includes two to solved variables.
	 */
	public static void lookupToSolveVariables(AbstractExpr aexpr, LinkedList<AbstractExpr> listAEVars, LinkedList<AbstractExpr> listAERootVars) throws JFCALCExpErrException	{
		/* assume listAERootVars is always not null.
		 * if (listAERootVars == null)	{
			listAERootVars = new LinkedList<AbstractExpr>();
		}*/
        // first of all, check if aexpr is one of AERootVars.
        for (int idx = 0; idx < listAERootVars.size(); idx ++)  {
            if (aexpr.isEqual(listAERootVars.get(idx)))     {
                boolean bVarHasBeenAdded = false;
                for (int idx2 = 0; idx2 < listAEVars.size(); idx2 ++)	{
                    if (aexpr.isEqual(listAEVars.get(idx2)))	{
                        bVarHasBeenAdded = true;	// this variable has been added
                        break;
                    }
                }
                if (!bVarHasBeenAdded)  {
                    listAEVars.add(aexpr);	// during running, aexpr may change. So have to deep copy b4 this function.
                }
                return;
            }
        }
		for (int idx1 = 0; idx1 < aexpr.getListOfChildren().size(); idx1 ++)	{
			for (int idx = 0; idx < listAERootVars.size(); idx ++)	{
				if (aexpr.getListOfChildren().get(idx1).isEqual(listAERootVars.get(idx)))	{
                    boolean bVarHasBeenAdded = false;
					for (int idx2 = 0; idx2 < listAEVars.size(); idx2 ++)	{
						if (aexpr.isEqual(listAEVars.get(idx2)))	{
							bVarHasBeenAdded = true;	// this variable has been added
                            break;
						}
					}
                    if (!bVarHasBeenAdded)  {
                        listAEVars.add(aexpr);
                    }
					return;
				}
			}
		}
        // none of its child is a root var, so check if it is a AEVar or not.
		if (aexpr.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE)	{
			for (int idx = 0; idx < listAEVars.size(); idx ++)	{
				if (aexpr.isEqual(listAEVars.get(idx)))	{
					return;
				}
			}
			listAEVars.add(aexpr);	// during running, aexpr may change. So have to deep copy b4 this function.
		} else	{
			// scan its children
			for (int idx = 0; idx < aexpr.getListOfChildren().size(); idx ++)	{
				lookupToSolveVariables(aexpr.getListOfChildren().get(idx), listAEVars, listAERootVars);
			}
		}
	}
	
	public static void lookupToSolveVarsInExprs(AbstractExpr[] aexprs, LinkedList<AbstractExpr> listAEVars, LinkedList<AbstractExpr> listAERootVars) throws JFCALCExpErrException, JSmartMathErrException	{
		if (listAERootVars == null)	{
			listAERootVars = new LinkedList<AbstractExpr>();
		}
		for (int idx = 0; idx < aexprs.length; idx ++)	{
            // during running, aexprs may change. So have to deep copy b4 this function.
			lookupToSolveVariables(aexprs[idx], listAEVars, listAERootVars);
		}
	}
	
	// length of aePatterns should be equal to length of datumValues. Also, parameter datumValues should not be changed inside the function.
	public static AbstractExpr calcExprValue(AbstractExpr aeOriginalExpr, AbstractExpr[] aeExprUnits, DataClass[] datumValues) throws JFCALCExpErrException, JSmartMathErrException	{
		LinkedList<PatternExprUnitMap> listFromToMap = new LinkedList<PatternExprUnitMap>();
		for (int idx = 0; idx < aeExprUnits.length; idx ++)	{
			AEConst aeTo = new AEConst(datumValues[idx].cloneSelf());
			PatternExprUnitMap pi = new PatternExprUnitMap(aeExprUnits[idx], aeTo);
			listFromToMap.add(pi);
		}
		AbstractExpr aeProcessedExpr = PatternManager.replaceExprPattern(aeOriginalExpr, listFromToMap, true);
		return aeProcessedExpr;
	}
	
	/*
	 * aeToSolve: the expression to solve pseudo constant, like f_aexpr_to_analyze(1, 2, -i) + f_aexpr_to_analyze(3, 1, 5) - 8;
	 * aeOriginalExpr: the expression f_aexpr_to_analyze(...) represents;
	 * listPEUMap: includes all the expression units which are looked on as variables and can be patterned.
	 */
	public static AbstractExpr replaceExprFunc4PCon(AbstractExpr aeToSolve, AbstractExpr aeOriginalExpr, LinkedList<PatternExprUnitMap> listPEUMap) throws JFCALCExpErrException, JSmartMathErrException, InterruptedException	{
		if (aeToSolve instanceof AEFunction
				&& ((AEFunction)aeToSolve).mstrFuncName.equalsIgnoreCase(FUNCTION_ORIGINAL_EXPRESSION))	{
			if (((AEFunction)aeToSolve).mlistChildren.size() != listPEUMap.size())	{
				throw new JSmartMathErrException(ERRORTYPES.ERROR_NUMBER_OF_VARIABLES_NOT_MATCH);
			}
			AbstractExpr[] aeExprUnits = new AbstractExpr[((AEFunction)aeToSolve).mlistChildren.size()];
			DataClass[] datumValues = new DataClass[((AEFunction)aeToSolve).mlistChildren.size()];
			for (int idx = 0; idx < ((AEFunction)aeToSolve).mlistChildren.size(); idx ++)	{
				// simplify it most to a constant.
                AbstractExpr aexprSimplified = ((AEFunction)aeToSolve).mlistChildren.get(idx).simplifyAExprMost(new LinkedList<UnknownVariable>(),
									new LinkedList<LinkedList<Variable>>(), new SimplifyParams(false, true, false));
				if (!(aexprSimplified instanceof AEConst))	{
					// currently only support constant parameters for f_aexpr_to_analyze
					throw new JSmartMathErrException(ERRORTYPES.ERROR_NOT_CONSTANT_ABSTRACTEXPR);
				}
				aeExprUnits[idx] = listPEUMap.get(idx).maeExprUnit;
				datumValues[idx] = ((AEConst) aexprSimplified).getDataClassRef();  // datumValues are only used in calcExprValue and they will not be changed inside the function.
			}
			aeToSolve = calcExprValue(aeOriginalExpr, aeExprUnits, datumValues);
		} else	{
			LinkedList<PatternExprUnitMap> listFromToMap = new LinkedList<PatternExprUnitMap>();
			LinkedList<AbstractExpr> listChildren = aeToSolve.getListOfChildren();
			for (int idx = 0; idx < listChildren.size(); idx ++)	{
				// does not matter if two children are the same. We just replace them together.
				PatternExprUnitMap pi = new PatternExprUnitMap(listChildren.get(idx),
						replaceExprFunc4PCon(listChildren.get(idx), aeOriginalExpr, listPEUMap));
				listFromToMap.add(pi);
			}
			aeToSolve = aeToSolve.replaceChildren(listFromToMap, true, new LinkedList<AbstractExpr>());
		}
		return aeToSolve;
	}
	
	public static boolean isSingleExprMatchPattern(AbstractExpr aeOriginalExpr,
												AbstractExpr aePatternExpr,
												LinkedList<UnknownVariable> listPseudoConstVars,	// pseudo constants name and value list of pattern
												LinkedList<PatternExprUnitMap> listPEUMap,
												LinkedList<UnknownVariable> listUnknown,	// unknown list of original expr
												LinkedList<LinkedList<Variable>> lVarNameSpaces)	// name spaces of original expr
								throws JFCALCExpErrException, JSmartMathErrException, InterruptedException	{
		// unknown2pattern is the unknown variable list specifically for pattern
		LinkedList<UnknownVariable> listUnknown2Pattern = new LinkedList<UnknownVariable>();
        listUnknown2Pattern.addAll(listUnknown);

        // have to convert aepatternExpr instead of aeOriginalExpr coz aeOriginalExpr may includes
        // some aexpr data which includes var's name some as pattern variable name. This will lead
        // to confusion later on.
		//aeOriginalExpr = PatternManager.replaceExprPattern(aeOriginalExpr, listPEUMap, true);
		aePatternExpr = PatternManager.replaceExprPattern(aePatternExpr, listPEUMap, false);
        // first, aeOriginalExpr seems to be simplified before so no need to simplify it further. Second, even if it is not simplified, we can still use it. 
        // pseudo const vars should be added in unknown var list coz aexpr might be simplified b4 pseudo const has value.
        listUnknown2Pattern.addAll(listPseudoConstVars);
		LinkedList<LinkedList<Variable>> lPatternVarNameSpaces = new LinkedList<LinkedList<Variable>>();
		aePatternExpr = aePatternExpr.simplifyAExprMost(listUnknown2Pattern, lPatternVarNameSpaces, new SimplifyParams(false, true, false));
		
		// simplify aeOriginalExpr - aePattern to see if it is zero or not.
		LinkedList<AbstractExpr> listAEChildren = new LinkedList<AbstractExpr>();
		listAEChildren.add(aeOriginalExpr);
		listAEChildren.add(aePatternExpr);
		LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
		listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true));
		listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2));		
		AbstractExpr aeOrginalMinusPattern = new AEPosNegOpt(listAEChildren, listOpts);
		aeOrginalMinusPattern = aeOrginalMinusPattern.simplifyAExprMost(listUnknown, lVarNameSpaces, new SimplifyParams(false, false, false));  // when we match back, we need to convert datum back to expr.
		if (aeOrginalMinusPattern.isNegligible())	{
			return true;
		} else	{
			return false;
		}
	}

	public boolean isPatternMatch(AbstractExpr[] arrayaeOriginalExprs,  // original exprs haven't been mostly simplifed coz need first call replace pattern then simplify most.
								LinkedList<AbstractExpr> listOriginalExprVars,
								LinkedList<PatternExprUnitMap> listPEUMap,	// the map of unknown variables to pattern units
								LinkedList<UnknownVariable> listPseudoConstVars,	// this list returns pseudo consts used in this pattern identifier.
								LinkedList<UnknownVariable> listUnknown,
								LinkedList<LinkedList<Variable>> lVarNameSpaces) throws JFCALCExpErrException, JSmartMathErrException, InterruptedException
	{
		if (arrayaeOriginalExprs.length != mpsaArray.length)	{
			return false;
		}
		AbstractExpr[] aearrayPatterns = new AbstractExpr[mpsaArray.length];
		for (int idx = 0; idx < mpsaArray.length; idx ++)	{
			aearrayPatterns[idx] = mpsaArray[idx].maePattern;
		}
		LinkedList<AbstractExpr> listPatternVars = new LinkedList<AbstractExpr>();
		lookupToSolveVarsInExprs(aearrayPatterns, listPatternVars, new LinkedList<AbstractExpr>());
		
		if (listOriginalExprVars.size() != listPatternVars.size())	{
			return false;	// original expression has not got same number of unknown variables as pattern
		} else if (arrayaeOriginalExprs.length != mpssArray.length)	{
			return false;	// number of expressions in pattern != number of original expressions
		} else	{
			// TODO: note, so far only support no order multiple variables, but should support ordered multiple variables later on.
			listPEUMap.clear();
			for (int idx = 0; idx < listOriginalExprVars.size(); idx ++)	{
				listPEUMap.add(new PatternExprUnitMap(listOriginalExprVars.get(idx), listPatternVars.get(idx)));
			}
		}
		
		int[] narrayPatternMapped = new int[aearrayPatterns.length];
		for (int idx = 0; idx < arrayaeOriginalExprs.length; idx ++)	{
			narrayPatternMapped[idx] = -1;	// means marrayaePatterns[idx] has not been mapped yet.
		}
		
		boolean bIsMatchPattern = true;
		for (int idx = 0; idx < arrayaeOriginalExprs.length; idx ++)	{
			boolean bIsSingleExprMatchPattern = false;
			for (int idx1 = 0; idx1 < aearrayPatterns.length; idx1 ++)	{
				if (narrayPatternMapped[idx1] != -1)	{
					continue;	// means marrayaePatterns[idx1] has been mapped to an original expression.
				}
				LinkedList<UnknownVariable> listLocalPConVars = new LinkedList<UnknownVariable>();
				LinkedList<UnknownVariable> listLocalPConVarsAll = new LinkedList<UnknownVariable>();
				boolean bPConValuesGot = true;
				for (int idx2 = 0; idx2 < mpsaArray[idx1].maePConExprs.length; idx2 ++)	{
					AbstractExpr aePConToSolve = replaceExprFunc4PCon(mpsaArray[idx1].maePConExprs[idx2],
							arrayaeOriginalExprs[idx], listPEUMap);
                    try {
                        aePConToSolve = aePConToSolve.simplifyAExprMost(listUnknown, lVarNameSpaces, new SimplifyParams(false, false, false));  // for pseudo consts, need to even simplify aexpr data otherwise may lead to infinite loop when match back.
                    } catch (JFCALCExpErrException e)  {
                        // it is possible that some parameters have incompatiable value type with the function.
                        // e.g. when try to match sind(x) to a polynomial, it may need to calculate sind(i) which
                        // is invalid because i cannot be parameter of sind.
                        return false;
                    }
					if (!(aePConToSolve instanceof AEConst)
							|| aePConToSolve.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_DATAREFVALUE)	{
						throw new JSmartMathErrException(ERRORTYPES.ERROR_PSEUDO_CONST_CANNOT_BE_EVALUATED);
					}
					UnknownVariable varPCon = UnknownVarOperator.lookUpList(
							((AEVar)mpsaArray[idx1].maePseudoConsts[idx2]).mstrVariableName,
							listPseudoConstVars);
					if (varPCon == null)	{
						listLocalPConVars.add(new UnknownVariable(
											((AEVar)mpsaArray[idx1].maePseudoConsts[idx2]).mstrVariableName,
											((AEConst)aePConToSolve).getDataClass()));  // getDataClass will automatically determine deep copy or not.
					} else if (((AEConst)aePConToSolve).getDataClassRef().isEqual(varPCon.getSolvedValue()) == false)	{
						bPConValuesGot = false;
						break;
					}
					listLocalPConVarsAll.add(new UnknownVariable(
							((AEVar)mpsaArray[idx1].maePseudoConsts[idx2]).mstrVariableName,
							((AEConst)aePConToSolve).getDataClass()));  // getDataClass will automatically determine deep copy or not.
				}
				if (!bPConValuesGot)	{
					continue;
				}
				// all the pseudo constant values obtained without problem, now verify back
				bIsSingleExprMatchPattern = isSingleExprMatchPattern(arrayaeOriginalExprs[idx],
						aearrayPatterns[idx1], listLocalPConVarsAll, listPEUMap, listUnknown, lVarNameSpaces);
				if (bIsSingleExprMatchPattern)	{
					// match
					narrayPatternMapped[idx1] = idx;
					// add all variables in the variable list only after we believe the single expr matches the pattern.
					for (int idx3 = 0; idx3 < listLocalPConVars.size(); idx3 ++)	{
						// pseudo constants should have been solved anyway
						DataClass datumValuePCon = listLocalPConVars.get(idx3).getSolvedValue();
                        if (datumValuePCon.getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR) {
                            AbstractExpr aexprData = datumValuePCon.getAExpr().simplifyAExprMost(listUnknown, lVarNameSpaces, new SimplifyParams(false, true, false));
                            if (aexprData instanceof AEConst && ((AEConst)aexprData).getDataClassRef().getDataType() != DATATYPES.DATUM_ABSTRACT_EXPR) {
                                datumValuePCon.copyTypeValue(((AEConst)aexprData).getDataClassRef());
                            } else {
                                datumValuePCon.setAExpr(aexprData);
                            }
                        }
						// if a pseudo constant is very very close to zero. We believe it is zero otherwise,
						// we may see more roots than we expect, considering a situation 0.000000000000000000000000000000000001 * x**6 + x+ 3 == 0,
						// where x**6 should be multiplied by zero.
						if (datumValuePCon.isZeros(false))	{
							DataClass datumZero = new DataClass();
							datumZero.setDataValue(MFPNumeric.ZERO);
							datumValuePCon.setAllLeafChildren(datumZero);
							listLocalPConVars.get(idx3).setValue(datumValuePCon);
						}
					}
					listPseudoConstVars.addAll(listLocalPConVars);
					break;
				}
			}
			if (!bIsSingleExprMatchPattern)	{
				// cannot find a match
				// TODO: here assume all pattern equations have same order level. If they have different
				// order level, have to repeat above search again.
				bIsMatchPattern = false;
				break;
			}
		}
		return bIsMatchPattern;
	}
	
	public DataClass[] solveOriginalExprUnit(PatternExprUnitMap peuMap, LinkedList<UnknownVariable> listPseudoConstVars)
			throws JFCALCExpErrException, InterruptedException, JSmartMathErrException	{
		AbstractExpr aePatternUnit = peuMap.maePatternUnit;
		for (int idx1 = 0; idx1 < marrayaeVars.length; idx1 ++)	{
			if (marrayaeVars[idx1].isEqual(aePatternUnit))	{
				// find!
				AbstractExpr aeReturn = marrayaeSolveVarExprs[idx1].simplifyAExprMost(listPseudoConstVars, new LinkedList<LinkedList<Variable>>(), new SimplifyParams(false, true, false));
				if (!(aeReturn instanceof AEConst))	{
					throw new JSmartMathErrException(ERRORTYPES.ERROR_VARIABLE_CANNOT_BE_SOLVED);
				}
				DataClass[] datumValueList;
				if (mbarrayRootsList[idx1] && ((AEConst)aeReturn).getDataClassRef().getDataType() == DATATYPES.DATUM_REF_DATA)	{
					datumValueList = ((AEConst)aeReturn).getDataClassRef().getDataList();   // because it is a matrix, so need not to call getDataClass
					return datumValueList;
				} else	{
					datumValueList = new DataClass[1];
					datumValueList[0] = ((AEConst)aeReturn).getDataClass(); // getDataClass will automatically determine when to deep copy.
					return datumValueList;
				}
			}
		}
		throw new JSmartMathErrException(ERRORTYPES.ERROR_VARIABLE_CANNOT_BE_SOLVED);
	}
	
	public LinkedList<DataClass[]> solveOriginalExprUnits(LinkedList<PatternExprUnitMap> listPEUMap,
			LinkedList<UnknownVariable> listPseudoConstVars) throws JFCALCExpErrException, InterruptedException, JSmartMathErrException	{
		LinkedList<DataClass[]> listReturnValues = new LinkedList<DataClass[]>();
		
		for (int idx = 0; idx < listPEUMap.size(); idx ++)	{
			DataClass[] datumReturnList = solveOriginalExprUnit(listPEUMap.get(idx), listPseudoConstVars);
			listReturnValues.add(datumReturnList);
		}
		return listReturnValues;
	}
}
