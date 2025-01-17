import { notFound } from 'next/navigation'
import { JSX } from 'react'
import { assertUnreachable } from '@/lib/assert'
import { db, sql } from '@/lib/db'
import { styled } from '@/lib/styled'
import {
	ChildNode,
	Node,
	Table,
	TableCell,
	TableHeading,
} from '@/lib/types/usfm'

const P = styled.p('leading-loose mb-4')

type ReaderProps = {
	passageRef: string
}

export async function Reader({ passageRef }: ReaderProps) {
	const passage = await getPassage(passageRef)

	return (
		<div className='text-gray-900 dark:text-gray-200 font-sans text-lg'>
			{passage.map((node, index) => (
				<ReaderNode key={index} node={node} />
			))}
		</div>
	)
}

function ReaderNode({ node }: { node: Node }) {
	const type = node[0]

	switch (type) {
		case 'cl': {
			return (
				<h2 className='mb-10 font-bold flex flex-col items-center'>
					<span className='block text-lg text-zinc-600 dark:text-zinc-400 mb-2'>
						{node[1]}
					</span>
					<span className='block text-7xl'>{node[2]}</span>
				</h2>
			)
		}

		case 's': {
			const Component = `h${node[1] + 2}` as keyof JSX.IntrinsicElements
			return (
				<Component className='text-2xl mb-4 mt-8 font-bold'>
					{node[2]}
				</Component>
			)
		}

		case 'ms':
		case 'd':
		case 'sp':
		case 'iex':
		case 'qa':
			return <p>{node[1]}</p>
		case 'p':
		case 'nb':
			return <P className='indent-2'>{renderChildren(node[1])}</P>
		case 'm':
		case 'pr':
		case 'cls':
		case 'pmo':
		case 'pmc':
		case 'pmr':
		case 'pm':
		case 'pc':
			return <P className='text-center'>{renderChildren(node[1])}</P>
		case 'qr':
			return <P className='text-right'>{renderChildren(node[1])}</P>
		case 'qc':
			return <P className='indent-2'>{renderChildren(node[1])}</P>

		case 'pi':
		case 'mi':
		case 'q':
		case 'qm':
		case 'li':
		case 'lim':
			return <p className='leading-loose mb-4'>{renderChildren(node[2])}</p>

		case 'b':
			return <div className='h-4' />

		case 'table':
			return <ReaderTable node={node} />

		default:
			assertUnreachable(node)
	}
}

function ReaderChildNode({ node }: { node: ChildNode }) {
	const [type, value] = node

	switch (type) {
		case 'v':
			return (
				<span className='text-gray-500 -top-2 relative align-baseline text-xs'>
					{value}&nbsp;
				</span>
			)

		case 'qs':
			return <span className='italic text-right'>{value}</span>

		case 'litl':
			return <span className='float-right'>{value}</span>

		case 'wj':
			return <span className='text-red-600 dark:text-red-500'>{value}</span>

		case 'em':
		case 'bd':
			return <span className='font-bold'>{value}</span>

		case 'bk':
		case 'qt':
		case 'sig':
		case 'sls':
		case 'tl':
		case 'it':
			return <span className='italic'>{value}</span>

		case 'nd':
		case 'sc':
			return <span className='[font-variant:small-caps]'>{value}</span>

		case 'sup':
			return <span className='align-super text-sm'>{value}</span>

		case 't':
			return <span>{value}</span>

		default:
			assertUnreachable(node)
	}
}

function renderChildren(nodes: ChildNode[]) {
	return nodes.map((node, index) => <ReaderChildNode key={index} node={node} />)
}

function ReaderTable({ node }: { node: Table }) {
	const { bodyRows, headerRows } = node[1].reduce(
		(acc, row) => {
			if (row.every((cell) => cell[0] === 'th')) {
				acc.headerRows.push(row as TableHeading[])
			} else {
				acc.bodyRows.push(row as TableCell[])
			}

			return acc
		},
		{
			bodyRows: [] as TableCell[][],
			headerRows: [] as TableHeading[][],
		},
	)

	return (
		<table>
			{headerRows ? (
				<thead>
					<tr>
						{headerRows.map((row, index) => (
							<tr key={index}>
								{row.map((cell, index) => (
									<th key={index}>{cell[1]}</th>
								))}
							</tr>
						))}
					</tr>
				</thead>
			) : null}

			<tbody>
				{bodyRows.map((row, index) => (
					<tr key={index}>
						{row.map((cell, index) => (
							<td key={index}>{cell[1]}</td>
						))}
					</tr>
				))}
			</tbody>
		</table>
	)
}

const getPassageQuery = db.prepare<
	{ ref: string },
	{ book: string; chapter: string; data: string }
>(
	sql`
    SELECT book.title as book, chapter.ref as chapter, chapter.data
    FROM chapter
    JOIN book ON book.ref = chapter.book_ref
    WHERE chapter.ref = @ref
  `,
)

async function getPassage(ref: string): Promise<Node[]> {
	const row = getPassageQuery.get({ ref })
	if (!row) {
		notFound()
	}

	const chapterNumber = row.chapter.split('.')[1]

	return [['cl', row.book, chapterNumber], ...JSON.parse(row.data)]
}
